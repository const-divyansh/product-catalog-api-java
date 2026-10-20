package com.example.catalog.product;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequests.Create request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) { return service.get(id); }

    @GetMapping
    public Page<ProductResponse> search(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        if (page < 0) throw new org.springframework.web.server.ResponseStatusException(
            HttpStatus.BAD_REQUEST, "page must be >= 0");
        if (size < 1 || size > 100) throw new org.springframework.web.server.ResponseStatusException(
            HttpStatus.BAD_REQUEST, "size must be between 1 and 100");
        return service.search(category, minPrice, maxPrice, q, page, size, sortBy, sortOrder);
    }

    @PutMapping("/{id}")
    public ProductResponse replace(@PathVariable Long id, @Valid @RequestBody ProductRequests.Create request) {
        return service.replace(id, request);
    }

    @PatchMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequests.Update request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
