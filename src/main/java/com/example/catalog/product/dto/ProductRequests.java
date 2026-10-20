package com.example.catalog.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class ProductRequests {
    private ProductRequests() {}

    @Schema(description = "Payload for creating or replacing a product")
    public record Create(
        @Schema(description = "Product title/name", example = "MacBook Pro 16")
        @NotBlank @Size(max = 255) String name,

        @Schema(description = "Detailed product description", example = "Apple M3 Pro, 36GB RAM, 512GB SSD")
        @Size(max = 10000) String description,

        @Schema(description = "Category name", example = "electronics")
        @NotBlank @Size(max = 100) String category,

        @Schema(description = "Price with up to 2 decimal places", example = "2499.00")
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,

        @Schema(description = "ISO 4217 3-letter currency code", example = "USD")
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currency
    ) {}

    @Schema(description = "Payload for partially updating a product")
    public record Update(
        @Schema(description = "Product title/name", example = "MacBook Pro 16 M3 Max")
        @Size(min = 1, max = 255) String name,

        @Schema(description = "Detailed product description")
        @Size(max = 10000) String description,

        @Schema(description = "Category name", example = "electronics")
        @Size(min = 1, max = 100) String category,

        @Schema(description = "Price with up to 2 decimal places", example = "2999.00")
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,

        @Schema(description = "ISO 4217 3-letter currency code", example = "USD")
        @Pattern(regexp = "[A-Za-z]{3}") String currency
    ) {}
}
