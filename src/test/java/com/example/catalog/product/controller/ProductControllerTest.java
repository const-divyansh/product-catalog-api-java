package com.example.catalog.product.controller;

import com.example.catalog.product.dto.ProductRequests;
import com.example.catalog.product.model.Product;
import com.example.catalog.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ProductController Integration Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUpDatabase() {
        productRepository.deleteAll();
    }

    private Product createProduct(String name, String category, BigDecimal price) {
        return productRepository.save(new Product(name, "Sample description", category, price, "USD"));
    }

    @Nested
    @DisplayName("POST /api/v1/products - Create Product")
    class CreateProductTests {

        @Test
        @DisplayName("should create product successfully when payload is valid")
        void shouldCreateProductWhenPayloadIsValid() throws Exception {
            var request = new ProductRequests.Create(
                "Running Shoes",
                "Comfortable road running shoes",
                "footwear",
                new BigDecimal("89.99"),
                "USD"
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Running Shoes")))
                .andExpect(jsonPath("$.category", is("footwear")))
                .andExpect(jsonPath("$.price", is(89.99)))
                .andExpect(jsonPath("$.currency", is("USD")))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
        }

        @Test
        @DisplayName("should return 400 Bad Request when required name is missing")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            var payload = Map.of(
                "name", "   ",
                "category", "electronics",
                "price", 19.99,
                "currency", "USD"
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("name"))));
        }

        @Test
        @DisplayName("should return 400 Bad Request when price is negative")
        void shouldReturn400WhenPriceIsNegative() throws Exception {
            var payload = Map.of(
                "name", "Mechanical Keyboard",
                "category", "electronics",
                "price", -10.00,
                "currency", "USD"
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("price"))));
        }

        @Test
        @DisplayName("should return 400 Bad Request when currency format is invalid")
        void shouldReturn400WhenCurrencyCodeIsInvalid() throws Exception {
            var payload = Map.of(
                "name", "Mechanical Keyboard",
                "category", "electronics",
                "price", 99.00,
                "currency", "US_DOLLAR"
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("currency"))));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id} - Retrieve Product by ID")
    class RetrieveProductTests {

        @Test
        @DisplayName("should return product details when product exists")
        void shouldReturnProductWhenFound() throws Exception {
            Product saved = createProduct("Trail Running Shoe", "footwear", new BigDecimal("120.00"));

            mockMvc.perform(get("/api/v1/products/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Trail Running Shoe")))
                .andExpect(jsonPath("$.price", is(120.00)));
        }

        @Test
        @DisplayName("should return 404 Not Found when product ID does not exist")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/products/{id}", 999999L))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products - List, Filter and Paginate Products")
    class ListAndFilterProductsTests {

        @BeforeEach
        void seedProducts() {
            createProduct("Budget Wireless Earbuds", "audio", new BigDecimal("29.99"));
            createProduct("Noise Canceling Over-Ear Headphones", "audio", new BigDecimal("199.99"));
            createProduct("Audiophile Studio Monitor Headphones", "audio", new BigDecimal("349.99"));
            createProduct("Ceramic Coffee Mug", "kitchen", new BigDecimal("14.50"));
        }

        @Test
        @DisplayName("should filter products by category")
        void shouldFilterByCategory() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                    .param("category", "audio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.content[*].category", everyItem(is("audio"))));
        }

        @Test
        @DisplayName("should filter products by minPrice and maxPrice range")
        void shouldFilterByPriceRange() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                    .param("minPrice", "20.00")
                    .param("maxPrice", "200.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder(
                    "Budget Wireless Earbuds",
                    "Noise Canceling Over-Ear Headphones"
                )));
        }

        @Test
        @DisplayName("should filter products by substring search query q")
        void shouldFilterBySearchQuery() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                    .param("q", "Headphones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder(
                    "Noise Canceling Over-Ear Headphones",
                    "Audiophile Studio Monitor Headphones"
                )));
        }

        @Test
        @DisplayName("should paginate and sort products correctly")
        void shouldPaginateAndSortProducts() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                    .param("category", "audio")
                    .param("page", "0")
                    .param("size", "2")
                    .param("sortBy", "price")
                    .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.numberOfElements", is(2)))
                .andExpect(jsonPath("$.content[0].price", is(29.99)))
                .andExpect(jsonPath("$.content[1].price", is(199.99)));
        }

        @Test
        @DisplayName("should return 400 Bad Request when minPrice is greater than maxPrice")
        void shouldReturn400WhenMinPriceExceedsMaxPrice() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                    .param("minPrice", "100.00")
                    .param("maxPrice", "50.00"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 Bad Request when page is negative")
        void shouldReturn400WhenPageIsNegative() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                    .param("page", "-1"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 Bad Request when page size exceeds upper limit of 100")
        void shouldReturn400WhenPageSizeExceedsMaximum() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                    .param("size", "150"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/products/{id} - Full Replacement")
    class ReplaceProductTests {

        @Test
        @DisplayName("should fully replace product attributes")
        void shouldReplaceProductWhenFound() throws Exception {
            Product saved = createProduct("Old Name", "old_category", new BigDecimal("10.00"));

            var replacement = new ProductRequests.Create(
                "Upgraded Name",
                "Upgraded Description",
                "upgraded_category",
                new BigDecimal("49.99"),
                "EUR"
            );

            mockMvc.perform(put("/api/v1/products/{id}", saved.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(replacement)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Upgraded Name")))
                .andExpect(jsonPath("$.category", is("upgraded_category")))
                .andExpect(jsonPath("$.price", is(49.99)))
                .andExpect(jsonPath("$.currency", is("EUR")));
        }

        @Test
        @DisplayName("should return 404 Not Found when replacing non-existent product")
        void shouldReturn404WhenReplacingNonExistentProduct() throws Exception {
            var replacement = new ProductRequests.Create(
                "Upgraded Name",
                "Upgraded Description",
                "upgraded_category",
                new BigDecimal("49.99"),
                "EUR"
            );

            mockMvc.perform(put("/api/v1/products/{id}", 888888L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(replacement)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/products/{id} - Partial Update")
    class UpdateProductTests {

        @Test
        @DisplayName("should partially update only specified fields")
        void shouldPartiallyUpdateProductFields() throws Exception {
            Product saved = createProduct("Ergonomic Desk Chair", "furniture", new BigDecimal("250.00"));

            var patchRequest = new ProductRequests.Update(
                null,
                "New mesh backrest description",
                null,
                new BigDecimal("219.99"),
                null
            );

            mockMvc.perform(patch("/api/v1/products/{id}", saved.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Ergonomic Desk Chair")))
                .andExpect(jsonPath("$.description", is("New mesh backrest description")))
                .andExpect(jsonPath("$.price", is(219.99)))
                .andExpect(jsonPath("$.category", is("furniture")));
        }

        @Test
        @DisplayName("should return 404 Not Found when patching non-existent product")
        void shouldReturn404WhenPatchingNonExistentProduct() throws Exception {
            var patchRequest = new ProductRequests.Update(
                null, null, null, new BigDecimal("100.00"), null
            );

            mockMvc.perform(patch("/api/v1/products/{id}", 777777L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{id} - Delete Product")
    class DeleteProductTests {

        @Test
        @DisplayName("should delete product and return 204 No Content")
        void shouldDeleteProductSuccessfully() throws Exception {
            Product saved = createProduct("Temporary Item", "general", new BigDecimal("5.00"));

            mockMvc.perform(delete("/api/v1/products/{id}", saved.getId()))
                .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/v1/products/{id}", saved.getId()))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 404 Not Found when deleting non-existent product")
        void shouldReturn404WhenDeletingNonExistentProduct() throws Exception {
            mockMvc.perform(delete("/api/v1/products/{id}", 666666L))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("OpenAPI & Swagger UI Endpoints")
    class SwaggerTests {

        @Test
        @DisplayName("should serve OpenAPI 3.0 v3/api-docs json schema")
        void shouldReturnOpenApiDocs() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", startsWith("3.")))
                .andExpect(jsonPath("$.info.title").value("Product Catalog REST API"))
                .andExpect(jsonPath("$.paths['/api/v1/products']").exists());
        }

        @Test
        @DisplayName("should serve Swagger UI html")
        void shouldReturnSwaggerUi() throws Exception {
            mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
        }
    }
}
