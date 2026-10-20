package com.example.catalog.product.service;

import com.example.catalog.product.dto.ProductRequests;
import com.example.catalog.product.dto.ProductResponse;
import com.example.catalog.product.exception.ProductNotFoundException;
import com.example.catalog.product.model.Product;
import com.example.catalog.product.repository.ProductRepository;
import com.example.catalog.product.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "name", "price", "createdAt");

    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProductResponse create(ProductRequests.Create request) {
        Product product = new Product(
            clean(request.name()),
            request.description(),
            clean(request.category()),
            request.price(),
            request.currency().toUpperCase(Locale.ROOT)
        );
        Product saved = repository.save(product);
        return ProductResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(findProductOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String category, BigDecimal minPrice, BigDecimal maxPrice,
                                        String q, int page, int size, String sortBy, String sortOrder) {
        validateSearchParameters(minPrice, maxPrice, sortBy);

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortOrder);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sortOrder must be asc or desc");
        }

        Sort sort = Sort.by(direction, sortBy);
        if (!"id".equals(sortBy)) {
            sort = sort.and(Sort.by(Sort.Direction.ASC, "id"));
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Product> spec = ProductSpecification.filter(category, minPrice, maxPrice, q);

        return repository.findAll(spec, pageable).map(ProductResponse::from);
    }

    @Override
    public ProductResponse replace(Long id, ProductRequests.Create request) {
        Product product = findProductOrThrow(id);
        product.replace(
            clean(request.name()),
            request.description(),
            clean(request.category()),
            request.price(),
            request.currency().toUpperCase(Locale.ROOT)
        );
        return ProductResponse.from(repository.save(product));
    }

    @Override
    public ProductResponse update(Long id, ProductRequests.Update request) {
        Product product = findProductOrThrow(id);
        product.update(
            request.name() == null ? null : clean(request.name()),
            request.description(),
            request.category() == null ? null : clean(request.category()),
            request.price(),
            request.currency() == null ? null : request.currency().toUpperCase(Locale.ROOT)
        );
        return ProductResponse.from(repository.save(product));
    }

    @Override
    public void delete(Long id) {
        Product product = findProductOrThrow(id);
        repository.delete(product);
    }

    private Product findProductOrThrow(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private void validateSearchParameters(BigDecimal minPrice, BigDecimal maxPrice, String sortBy) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice must be <= maxPrice");
        }
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported sortBy");
        }
    }

    private static String clean(String value) {
        return value.trim();
    }
}
