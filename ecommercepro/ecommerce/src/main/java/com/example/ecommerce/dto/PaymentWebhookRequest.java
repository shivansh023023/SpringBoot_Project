package com.example.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentWebhookRequest {
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    private String event; // e.g., "payment.captured"
    private Payload payload;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {
        private PaymentEntity payment;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentEntity {
        private Entity entity;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entity {
        private String id;       // Razorpay Payment ID

        @JsonProperty("order_id")
        private String orderId;  // The Razorpay Order ID

        private String status;   // "captured", "failed"
    }
}