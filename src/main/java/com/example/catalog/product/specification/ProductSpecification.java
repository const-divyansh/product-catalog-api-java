package com.example.catalog.product.specification;

import com.example.catalog.product.model.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> filter(String category, BigDecimal minPrice, BigDecimal maxPrice, String searchKeyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category.trim()));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (searchKeyword != null && !searchKeyword.isBlank()) {
                String escapedPattern = "%" + escapeLike(searchKeyword.trim().toLowerCase(Locale.ROOT)) + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), escapedPattern, '\\'));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
