package com.example.ecommerce.repository;

import com.example.ecommerce.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<CartItem, String> {

    // Get all items in a user's cart
    List<CartItem> findByUserId(String userId);

    // Find specific item to check if it already exists in cart
    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);

    // Clear cart (used after order placement)
    void deleteByUserId(String userId);
}