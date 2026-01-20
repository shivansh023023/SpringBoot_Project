package com.example.ecommerce.dto;

import com.example.ecommerce.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {

    private String id;
    private String userId;
    private Double totalAmount;
    private String status;

    // Exact requested structure for Payment
    private PaymentSummary payment;

    // We can reuse the Entity for items since the structure matches
    private List<OrderItem> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentSummary {
        private String id;
        private String status;
        private Double amount;
    }
}