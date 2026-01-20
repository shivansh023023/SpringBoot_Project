package com.example.ecommerce.controller;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public CartItem addToCart(@Valid @RequestBody AddToCartRequest request) {
        return cartService.addToCart(request);
    }

    @GetMapping("/{userId}")
    public List<com.example.ecommerce.dto.CartResponseDTO> getCart(@PathVariable String userId) {
        return cartService.getCartByUserId(userId);
    }

    @DeleteMapping("/{userId}/clear")
    public java.util.Map<String, String> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);

        // Return simple JSON: { "message": "Cart cleared successfully" }
        return java.util.Collections.singletonMap("message", "Cart cleared successfully");
    }
}