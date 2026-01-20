package com.example.ecommerce.service;

import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.dto.PaymentWebhookRequest; // Ensure this import exists
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate; // Required for self-call

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    // Remove @Autowired. Create it manually.
    private RestTemplate restTemplate = new RestTemplate();// Used to call our own webhook

    public Payment createPayment(PaymentRequest request) {
        // 1. Validate Order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"CREATED".equals(order.getStatus())) {
            throw new RuntimeException("Order is not in CREATED state");
        }

        // 2. Create Payment Record (Pending)
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setStatus("PENDING");
        payment.setCreatedAt(Instant.now());
        payment.setPaymentId("pay_mock_" + UUID.randomUUID().toString().substring(0, 8));

        Payment savedPayment = paymentRepository.save(payment);

        // =================================================================================
        // 3. MOCK BANK LOGIC (Internal Self-Trigger)
        // Run in a separate thread so we return the "PENDING" response immediately to the user
        // =================================================================================
        CompletableFuture.runAsync(() -> {
            try {
                // A. Wait 3 seconds
                TimeUnit.SECONDS.sleep(3);

                // B. Construct the Webhook Payload (Exact Razorpay Structure)
                PaymentWebhookRequest webhookRequest = new PaymentWebhookRequest();
                webhookRequest.setEvent("payment.captured");

                PaymentWebhookRequest.Payload payload = new PaymentWebhookRequest.Payload();
                PaymentWebhookRequest.PaymentEntity paymentEntity = new PaymentWebhookRequest.PaymentEntity();
                PaymentWebhookRequest.Entity entity = new PaymentWebhookRequest.Entity();

                entity.setId(savedPayment.getPaymentId());
                entity.setOrderId(savedPayment.getOrderId());
                entity.setStatus("captured"); // This maps to SUCCESS

                paymentEntity.setEntity(entity);
                payload.setPayment(paymentEntity);
                webhookRequest.setPayload(payload);

                // C. Call OUR OWN Webhook Endpoint
                String webhookUrl = "http://localhost:8080/api/webhooks/payment";
                restTemplate.postForObject(webhookUrl, webhookRequest, String.class);

                System.out.println("✅ AUTO-TRIGGER: Webhook fired for Order " + savedPayment.getOrderId());

            } catch (Exception e) {
                System.err.println("❌ AUTO-TRIGGER FAILED: " + e.getMessage());
                e.printStackTrace();
            }
        });

        return savedPayment;
    }

    // This is called by the Webhook Controller (which was triggered by the code above)
    public void updatePaymentStatus(String orderId, String status) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order"));

        payment.setStatus(status);
        paymentRepository.save(payment);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("SUCCESS".equals(status)) {
            order.setStatus("PAID");
        } else {
            order.setStatus("PAYMENT_FAILED");
        }
        orderRepository.save(order);
    }
}