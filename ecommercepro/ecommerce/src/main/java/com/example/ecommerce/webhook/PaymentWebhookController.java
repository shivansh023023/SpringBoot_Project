package com.example.ecommerce.webhook;

import com.example.ecommerce.dto.PaymentWebhookRequest;
import com.example.ecommerce.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class PaymentWebhookController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/payment")
    public void handlePaymentWebhook(@RequestBody PaymentWebhookRequest request) {
        // Extract necessary data from the nested Mock/Razorpay JSON structure
        // Assuming structure: payload -> payment -> entity -> order_id, status

        String orderId = request.getPayload()
                .getPayment()
                .getEntity()
                .getOrderId();

        // In Razorpay, status is "captured". In Mock, we might send "SUCCESS".
        // We normalize this to "SUCCESS" for our service layer.
        String status = request.getPayload()
                .getPayment()
                .getEntity()
                .getStatus();

        String internalStatus = "captured".equalsIgnoreCase(status) || "SUCCESS".equalsIgnoreCase(status)
                ? "SUCCESS"
                : "FAILED";

        paymentService.updatePaymentStatus(orderId, internalStatus);
    }
}