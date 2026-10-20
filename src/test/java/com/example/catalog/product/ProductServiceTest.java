package com.example.catalog.product;

import com.example.catalog.product.exception.ProductNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Nested
    @DisplayName("Create Product Tests")
    class CreateTests {

        @Test
        @DisplayName("should persist entity and return mapped response")
        void shouldCreateProduct() {
            var request = new ProductRequests.Create(
                "Mechanical Keyboard",
                "Tactile mechanical switch keyboard",
                "accessories",
                new BigDecimal("89.99"),
                "usd"
            );

            Product savedEntity = new Product(
                "Mechanical Keyboard",
                "Tactile mechanical switch keyboard",
                "accessories",
                new BigDecimal("89.99"),
                "USD"
            );

            given(productRepository.save(any(Product.class))).willReturn(savedEntity);

            ProductResponse response = productService.create(request);

            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("Mechanical Keyboard");
            assertThat(response.currency()).isEqualTo("USD");
            then(productRepository).should().save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Retrieve Product Tests")
    class RetrieveTests {

        @Test
        @DisplayName("should return mapped product response when product exists")
        void shouldReturnProductWhenFound() {
            Product entity = new Product("Ergonomic Mouse", "Wireless mouse", "accessories", new BigDecimal("49.99"), "USD");
            given(productRepository.findById(1L)).willReturn(Optional.of(entity));

            ProductResponse response = productService.get(1L);

            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo("Ergonomic Mouse");
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when product ID is not found")
        void shouldThrowExceptionWhenNotFound() {
            given(productRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.get(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");
        }
    }

    @Nested
    @DisplayName("Search & Filter Validation Tests")
    class SearchTests {

        @Test
        @DisplayName("should query repository with specification and pageable")
        void shouldSearchProductsSuccessfully() {
            Product product = new Product("Smart Watch", "Fitness watch", "wearables", new BigDecimal("199.99"), "USD");
            Page<Product> page = new PageImpl<>(List.of(product));
            given(productRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);

            Page<ProductResponse> result = productService.search(
                "wearables", new BigDecimal("100.00"), new BigDecimal("300.00"),
                "watch", 0, 10, "price", "asc"
            );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("Smart Watch");
            then(productRepository).should().findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("should throw ResponseStatusException when minPrice is greater than maxPrice")
        void shouldThrowExceptionWhenMinPriceExceedsMaxPrice() {
            assertThatThrownBy(() -> productService.search(
                "wearables", new BigDecimal("500.00"), new BigDecimal("100.00"),
                null, 0, 10, "price", "asc"
            ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("minPrice must be <= maxPrice");

            then(productRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("should throw ResponseStatusException when sortBy is not an allowed field")
        void shouldThrowExceptionWhenSortByIsInvalid() {
            assertThatThrownBy(() -> productService.search(
                null, null, null, null, 0, 10, "unsupportedColumn", "asc"
            ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Unsupported sortBy");

            then(productRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("should throw ResponseStatusException when sortOrder is neither asc nor desc")
        void shouldThrowExceptionWhenSortOrderIsInvalid() {
            assertThatThrownBy(() -> productService.search(
                null, null, null, null, 0, 10, "price", "invalidDirection"
            ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("sortOrder must be asc or desc");

            then(productRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("Update & Delete Tests")
    class UpdateAndDeleteTests {

        @Test
        @DisplayName("should replace product details when product exists")
        void shouldReplaceProduct() {
            Product existing = new Product("Old Name", "Old Desc", "old_cat", new BigDecimal("10.00"), "USD");
            given(productRepository.findById(1L)).willReturn(Optional.of(existing));
            given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

            var updateRequest = new ProductRequests.Create(
                "New Name", "New Desc", "new_cat", new BigDecimal("25.00"), "EUR"
            );

            ProductResponse response = productService.replace(1L, updateRequest);

            assertThat(response.name()).isEqualTo("New Name");
            assertThat(response.category()).isEqualTo("new_cat");
            assertThat(response.currency()).isEqualTo("EUR");
            then(productRepository).should().save(existing);
        }

        @Test
        @DisplayName("should delete product entity when product exists")
        void shouldDeleteProduct() {
            Product existing = new Product("Item", "Desc", "cat", new BigDecimal("10.00"), "USD");
            given(productRepository.findById(1L)).willReturn(Optional.of(existing));

            productService.delete(1L);

            then(productRepository).should().delete(existing);
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when deleting non-existent product")
        void shouldThrowExceptionWhenDeletingNonExistentProduct() {
            given(productRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.delete(1L))
                .isInstanceOf(ProductNotFoundException.class);

            then(productRepository).should(never()).delete(any(Product.class));
        }
    }
}
