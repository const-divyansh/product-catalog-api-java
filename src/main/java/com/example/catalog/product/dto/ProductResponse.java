package com.example.catalog.product.dto;

import com.example.catalog.product.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Product representation response")
public record ProductResponse(
    @Schema(description = "Unique ID", example = "1") Long id,
    @Schema(description = "Product name", example = "MacBook Pro 16") String name,
    @Schema(description = "Product description", example = "Apple M3 Pro, 36GB RAM, 512GB SSD") String description,
    @Schema(description = "Product category", example = "electronics") String category,
    @Schema(description = "Product price", example = "2499.00") BigDecimal price,
    @Schema(description = "ISO 4217 Currency", example = "USD") String currency,
    @Schema(description = "Creation timestamp") Instant createdAt,
    @Schema(description = "Last update timestamp") Instant updatedAt
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getCategory(),
            p.getPrice(), p.getCurrency(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
