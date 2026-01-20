package com.example.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDTO {

    private String id;
    private String productId;
    private Integer quantity;

    // Nested DTO to restrict Product fields
    private ProductSummary product;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductSummary {
        private String id;
        private String name;
        private Double price;
    }
}