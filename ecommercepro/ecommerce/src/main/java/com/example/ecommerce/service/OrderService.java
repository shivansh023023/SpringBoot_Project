package com.example.ecommerce.service;

import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    public Order createOrder(String userId) {
        // 1. Get Cart Items
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        double totalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        // 2. Validate Stock & Calculate Total
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Out of stock: " + product.getName());
            }

            // Calculate price snapshot
            totalAmount += product.getPrice() * cartItem.getQuantity();

            // Prepare Order Item (POJO)
            OrderItem orderItem = new OrderItem(
                    product.getId(),
                    cartItem.getQuantity(),
                    product.getPrice()
            );
            orderItems.add(orderItem);
        }

        // 3. Create & Save Order
        Order order = new Order();
        order.setUserId(userId);
        order.setItems(orderItems); // Embedded list
        order.setTotalAmount(totalAmount);
        order.setStatus("CREATED");
        order.setCreatedAt(Instant.now());

        Order savedOrder = orderRepository.save(order);

        // 4. Deduct Stock
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId()).get();
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // 5. Clear Cart
        cartRepository.deleteByUserId(userId);

        return savedOrder;
    }

    @Autowired
    private com.example.ecommerce.repository.PaymentRepository paymentRepository; // Ensure this is injected

    // Change return type to OrderResponseDTO
    public com.example.ecommerce.dto.OrderResponseDTO getOrderById(String orderId) {
        // 1. Fetch Order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2. Fetch Payment
        com.example.ecommerce.model.Payment paymentEntity = paymentRepository.findByOrderId(orderId).orElse(null);

        // 3. Map Payment to Nested DTO
        com.example.ecommerce.dto.OrderResponseDTO.PaymentSummary paymentSummary = null;
        if (paymentEntity != null) {
            paymentSummary = new com.example.ecommerce.dto.OrderResponseDTO.PaymentSummary(
                    paymentEntity.getId(),
                    paymentEntity.getStatus(),
                    paymentEntity.getAmount()
            );
        }

        // 4. Return Final DTO
        return new com.example.ecommerce.dto.OrderResponseDTO(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getStatus(),
                paymentSummary,
                order.getItems()
        );
    }
}