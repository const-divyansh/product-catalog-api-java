package com.example.catalog.product.controller;

import com.example.catalog.product.dto.ProductRequests;
import com.example.catalog.product.dto.ProductResponse;
import com.example.catalog.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Endpoints for managing and querying e-commerce catalog products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a product", description = "Adds a new product to the catalog with mandatory validation.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    public ProductResponse create(@Valid @RequestBody ProductRequests.Create request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves details of a specific product by its unique identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ProductResponse get(
        @Parameter(description = "Product unique ID", example = "1")
        @PathVariable Long id
    ) {
        return service.get(id);
    }

    @GetMapping
    @Operation(
        summary = "Search, filter, and paginate products",
        description = "High-throughput search and filtering across categories, price boundaries, and text matching with index-backed pagination."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paginated list of products"),
        @ApiResponse(responseCode = "400", description = "Invalid query parameters (e.g. negative page, invalid sort field)")
    })
    public Page<ProductResponse> search(
        @Parameter(description = "Filter by exact category name", example = "electronics")
        @RequestParam(required = false) String category,

        @Parameter(description = "Minimum price filter (inclusive)", example = "100.00")
        @RequestParam(required = false) BigDecimal minPrice,

        @Parameter(description = "Maximum price filter (inclusive)", example = "1500.00")
        @RequestParam(required = false) BigDecimal maxPrice,

        @Parameter(description = "Case-insensitive text search in product name", example = "pro")
        @RequestParam(required = false) String q,

        @Parameter(description = "Zero-based page index", example = "0")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "Page size (1 to 100)", example = "20")
        @RequestParam(defaultValue = "20") int size,

        @Parameter(description = "Sort field ('price', 'name', or 'createdAt')", example = "price")
        @RequestParam(defaultValue = "createdAt") String sortBy,

        @Parameter(description = "Sort direction ('asc' or 'desc')", example = "asc")
        @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be >= 0");
        }
        if (size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be between 1 and 100");
        }
        return service.search(category, minPrice, maxPrice, q, page, size, sortBy, sortOrder);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace product", description = "Idempotent full replacement of an existing product entity.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product replaced successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ProductResponse replace(
        @Parameter(description = "Product unique ID", example = "1")
        @PathVariable Long id,
        @Valid @RequestBody ProductRequests.Create request
    ) {
        return service.replace(id, request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partial update product", description = "Updates specific non-null fields of a product without altering unspecified fields.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ProductResponse update(
        @Parameter(description = "Product unique ID", example = "1")
        @PathVariable Long id,
        @Valid @RequestBody ProductRequests.Update request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete product", description = "Removes a product from the catalog by its ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public void delete(
        @Parameter(description = "Product unique ID", example = "1")
        @PathVariable Long id
    ) {
        service.delete(id);
    }
}
