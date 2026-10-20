package com.example.catalog.product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
    Long id, String name, String description, String category,
    BigDecimal price, String currency, Instant createdAt, Instant updatedAt
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getCategory(),
            p.getPrice(), p.getCurrency(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
