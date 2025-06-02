package com.omega.retail.controller;

import com.omega.retail.dto.request.ProductProviderRequest;
import com.omega.retail.dto.response.ProductProviderResponse;
import com.omega.retail.service.ProductProviderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-providers")
public class ProductProviderController {

    private final ProductProviderService productProviderService;

    @Autowired
    public ProductProviderController(ProductProviderService productProviderService) {
        this.productProviderService = productProviderService;
    }

    @GetMapping
    public ResponseEntity<List<ProductProviderResponse>> getAll() {
        List<ProductProviderResponse> providers = productProviderService.getAll();
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductProviderResponse> getById(@PathVariable Long id) {
        ProductProviderResponse provider = productProviderService.getById(id);
        if (provider == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(provider);
    }

    @PostMapping
    public ResponseEntity<ProductProviderResponse> create(@RequestBody  @Valid ProductProviderRequest request) {
        ProductProviderResponse created = productProviderService.create(request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductProviderResponse> update(@PathVariable Long id, @RequestBody ProductProviderRequest request) {
        ProductProviderResponse updated = productProviderService.update(id, request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = productProviderService.delete(id);
        if (deleted) {
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    @PutMapping("/set-default/{id}")
    public ResponseEntity<String> setDefaultProvider(@PathVariable Long id){
        productProviderService.setDefaultProvider(id);
        return ResponseEntity.ok("Provider set default correctly");
    }
}


