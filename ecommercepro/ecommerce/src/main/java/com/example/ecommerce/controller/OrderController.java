package com.example.ecommerce.controller;

import com.example.ecommerce.dto.CreateOrderRequest;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Order createOrder(@Valid @RequestBody CreateOrderRequest request) {
        // ADAPTER PATTERN: Extract userId from DTO to pass to original Service
        return orderService.createOrder(request.getUserId());
    }

    @GetMapping("/{orderId}")
    public com.example.ecommerce.dto.OrderResponseDTO getOrder(@PathVariable String orderId) {
        return orderService.getOrderById(orderId);
    }
}