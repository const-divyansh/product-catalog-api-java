package com.example.catalog.product.repository;

import com.example.catalog.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Optional;

import static com.example.catalog.product.specification.ProductSpecification.filter;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ProductRepository Integration Tests")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Product laptop;

    @BeforeEach
    void setUp() {
        laptop = entityManager.persist(new Product("MacBook Pro 16", "High-performance laptop", "electronics", new BigDecimal("2499.00"), "USD"));
        entityManager.persist(new Product("Pixel 9 Pro", "Google flagship smartphone", "electronics", new BigDecimal("999.00"), "USD"));
        entityManager.persist(new Product("Road Runner 2", "Lightweight marathon running shoes", "footwear", new BigDecimal("129.99"), "USD"));
        entityManager.flush();
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("should find product by id when entity exists")
        void shouldFindProductByIdWhenExists() {
            Optional<Product> found = productRepository.findById(laptop.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("MacBook Pro 16");
            assertThat(found.get().getCategory()).isEqualTo("electronics");
            assertThat(found.get().getPrice()).isEqualByComparingTo("2499.00");
        }

        @Test
        @DisplayName("should persist new product with auto-generated timestamps")
        void shouldPersistNewProductWithTimestamps() {
            Product keyboard = new Product("Mechanical Keyboard", "RGB Gaming keyboard", "accessories", new BigDecimal("89.50"), "USD");

            Product saved = productRepository.save(keyboard);
            entityManager.flush();

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should delete product entity successfully")
        void shouldDeleteProduct() {
            productRepository.delete(laptop);
            entityManager.flush();

            Optional<Product> found = productRepository.findById(laptop.getId());
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Specification Queries (Filtering & Search)")
    class SpecificationQueries {

        @Test
        @DisplayName("should filter products strictly by category")
        void shouldFilterStrictlyByCategory() {
            Specification<Product> spec = filter("electronics", null, null, null);
            Page<Product> page = productRepository.findAll(spec, PageRequest.of(0, 10));

            assertThat(page.getTotalElements()).isEqualTo(2);
            assertThat(page.getContent())
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("MacBook Pro 16", "Pixel 9 Pro");
        }

        @Test
        @DisplayName("should filter products within minPrice and maxPrice range")
        void shouldFilterByPriceRange() {
            Specification<Product> spec = filter(null, new BigDecimal("100.00"), new BigDecimal("1000.00"), null);
            Page<Product> page = productRepository.findAll(spec, PageRequest.of(0, 10));

            assertThat(page.getTotalElements()).isEqualTo(2);
            assertThat(page.getContent())
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Pixel 9 Pro", "Road Runner 2");
        }

        @Test
        @DisplayName("should perform case-insensitive substring search on name")
        void shouldPerformSubstringSearch() {
            Specification<Product> spec = filter(null, null, null, "pro");
            Page<Product> page = productRepository.findAll(spec, PageRequest.of(0, 10));

            assertThat(page.getTotalElements()).isEqualTo(2);
            assertThat(page.getContent())
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("MacBook Pro 16", "Pixel 9 Pro");
        }

        @Test
        @DisplayName("should paginate and sort results by price descending")
        void shouldPaginateAndSortByPriceDesc() {
            Specification<Product> spec = filter(null, null, null, null);
            Page<Product> page = productRepository.findAll(
                spec,
                PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "price"))
            );

            assertThat(page.getTotalElements()).isEqualTo(3);
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getContent().get(0).getName()).isEqualTo("MacBook Pro 16");
            assertThat(page.getContent().get(1).getName()).isEqualTo("Pixel 9 Pro");
        }
    }
}
