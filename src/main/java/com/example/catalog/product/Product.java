package com.example.catalog.product;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_products_category_price_id", columnList = "category,price,id"),
    @Index(name = "idx_products_created_at_id", columnList = "created_at,id")
})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 10000)
    private String description;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    protected Product() {}

    public Product(String name, String description, String category, BigDecimal price, String currency) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.currency = currency;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void replace(String name, String description, String category, BigDecimal price, String currency) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.currency = currency;
    }

    public void update(String name, String description, String category, BigDecimal price, String currency) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (category != null) this.category = category;
        if (price != null) this.price = price;
        if (currency != null) this.currency = currency;
    }
}
