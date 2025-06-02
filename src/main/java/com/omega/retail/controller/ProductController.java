package com.omega.retail.controller;

import com.omega.retail.dto.request.ProductRequest;
import com.omega.retail.dto.response.ProductResponse;
import com.omega.retail.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.status(204).build();
    }

    @GetMapping("/belowSecurityStock")
    public ResponseEntity<List<ProductResponse>> getProductsBelowSecurityStock() {
        return ResponseEntity.ok(productService.getProductsBelowSecurityStock());
    }
    @GetMapping("/belowReorderPoint")
    public ResponseEntity<List<ProductResponse>> getProductsBelowReorderPoint() {
        return ResponseEntity.ok(productService.getProductsBelowReorderPointWithoutPendingOrders());
    }
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<ProductResponse>> getActiveProductsByProvider(@PathVariable Long providerId) {
        List<ProductResponse> products = productService.getActiveProductsByProvider(providerId);
        return ResponseEntity.ok(products);
    }
}
