package com.example.catalog.product;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) { this.repository = repository; }

    public ProductResponse create(ProductRequests.Create request) {
        Product product = new Product(clean(request.name()), request.description(),
            clean(request.category()), request.price(), request.currency().toUpperCase(Locale.ROOT));
        return ProductResponse.from(repository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String category, BigDecimal minPrice, BigDecimal maxPrice,
                                        String q, int page, int size, String sortBy, String sortOrder) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice must be <= maxPrice");
        }

        List<String> allowedSorts = List.of("id", "name", "price", "createdAt");
        if (!allowedSorts.contains(sortBy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported sortBy");
        }
        Sort.Direction direction;
        try { direction = Sort.Direction.fromString(sortOrder); }
        catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sortOrder must be asc or desc");
        }

        Sort sort = Sort.by(direction, sortBy);
        if (!sortBy.equals("id")) sort = sort.and(Sort.by(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null && !category.isBlank())
                predicates.add(cb.equal(root.get("category"), category));
            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            if (q != null && !q.isBlank())
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + escapeLike(q.toLowerCase(Locale.ROOT)) + "%", '\\'));
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        return repository.findAll(spec, pageable).map(ProductResponse::from);
    }

    public ProductResponse replace(Long id, ProductRequests.Create request) {
        Product product = findOrThrow(id);
        product.replace(clean(request.name()), request.description(), clean(request.category()),
            request.price(), request.currency().toUpperCase(Locale.ROOT));
        return ProductResponse.from(repository.save(product));
    }

    public ProductResponse update(Long id, ProductRequests.Update request) {
        Product product = findOrThrow(id);
        product.update(request.name() == null ? null : clean(request.name()),
            request.description(), request.category() == null ? null : clean(request.category()),
            request.price(), request.currency() == null ? null : request.currency().toUpperCase(Locale.ROOT));
        return ProductResponse.from(repository.save(product));
    }

    public void delete(Long id) {
        Product product = findOrThrow(id);
        repository.delete(product);
    }

    private Product findOrThrow(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private static String clean(String value) { return value.trim(); }
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
