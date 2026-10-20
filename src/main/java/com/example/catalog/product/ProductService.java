package com.example.catalog.product;

import org.springframework.data.domain.Page;
import java.math.BigDecimal;

/**
 * Product Service Interface following Interface Segregation and Dependency Inversion Principles.
 */
public interface ProductService {

    ProductResponse create(ProductRequests.Create request);

    ProductResponse get(Long id);

    Page<ProductResponse> search(String category, BigDecimal minPrice, BigDecimal maxPrice,
                                  String q, int page, int size, String sortBy, String sortOrder);

    ProductResponse replace(Long id, ProductRequests.Create request);

    ProductResponse update(Long id, ProductRequests.Update request);

    void delete(Long id);
}
