package com.example.catalog.product;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class ProductRequests {
    private ProductRequests() {}

    public record Create(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 10000) String description,
        @NotBlank @Size(max = 100) String category,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currency
    ) {}

    public record Update(
        @Size(min = 1, max = 255) String name,
        @Size(max = 10000) String description,
        @Size(min = 1, max = 100) String category,
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
        @Pattern(regexp = "[A-Za-z]{3}") String currency
    ) {}
}
