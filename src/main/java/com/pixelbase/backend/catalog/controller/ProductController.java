package com.pixelbase.backend.catalog.controller;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pixelbase.backend.catalog.dto.ProductRequest;
import com.pixelbase.backend.catalog.entity.CategoryEntity;
import com.pixelbase.backend.catalog.entity.ProductEntity;
import com.pixelbase.backend.catalog.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Page<ProductEntity> search(@RequestParam(required = false) String q,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return productService.search(q, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public ProductEntity get(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ProductEntity> create(@Valid @RequestBody ProductRequest req) {
        ProductEntity p = new ProductEntity();
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        if (req.getCategoryIds() != null) {
            List<CategoryEntity> cats = req.getCategoryIds().stream().map(id -> {
                CategoryEntity c = new CategoryEntity();
                c.setId(id);
                return c;
            }).collect(Collectors.toList());
            p.setCategories(cats);
        }
        ProductEntity saved = productService.create(p);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductEntity> update(@PathVariable Long id, @Valid @RequestBody ProductRequest req) {
        ProductEntity p = new ProductEntity();
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        if (req.getCategoryIds() != null) {
            List<CategoryEntity> cats = req.getCategoryIds().stream().map(cid -> {
                CategoryEntity c = new CategoryEntity();
                c.setId(cid);
                return c;
            }).collect(Collectors.toList());
            p.setCategories(cats);
        }
        ProductEntity updated = productService.update(id, p);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
