package com.example.ecommerce.service;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    public CartItem addToCart(AddToCartRequest request) {
        // 1. Validate Product Exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 2. Check Stock Availability
        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        // 3. Check if item already in cart
        Optional<CartItem> existingItem = cartRepository.findByUserIdAndProductId(
                request.getUserId(),
                request.getProductId()
        );

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            return cartRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUserId(request.getUserId());
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            return cartRepository.save(newItem);
        }
    }


    public List<com.example.ecommerce.dto.CartResponseDTO> getCartByUserId(String userId) {
        List<CartItem> cartItems = cartRepository.findByUserId(userId);

        return cartItems.stream().map(item -> {
            // 1. Fetch Product
            Product p = productRepository.findById(item.getProductId()).orElse(null);

            // 2. Map to Nested DTO (ProductSummary)
            com.example.ecommerce.dto.CartResponseDTO.ProductSummary productSummary = null;
            if (p != null) {
                productSummary = new com.example.ecommerce.dto.CartResponseDTO.ProductSummary(
                        p.getId(),
                        p.getName(),
                        p.getPrice()
                );
            }

            // 3. Map to Main DTO
            return new com.example.ecommerce.dto.CartResponseDTO(
                    item.getId(),
                    item.getProductId(),
                    item.getQuantity(),
                    productSummary
            );
        }).collect(java.util.stream.Collectors.toList());
    }

    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }
}