package com.omega.retail.controller;
import com.omega.retail.dto.request.ProviderRequest;
import com.omega.retail.dto.response.ProviderResponse;
import com.omega.retail.service.ProviderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/providers")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @PostMapping
    public ResponseEntity<ProviderResponse> createProvider(@RequestBody @Valid ProviderRequest request) {
        ProviderResponse response = providerService.createProvider(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProviderResponse> getProvider(@PathVariable Long id) {
        ProviderResponse response = providerService.getProvider(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProviderResponse>> getAllProviders() {
        List<ProviderResponse> list = providerService.getAllProviders();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProviderResponse>> getProvidersNotDeactivated() {
        List<ProviderResponse> response = providerService.getProvidersNotDeactivated();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProviderResponse> updateProvider(@PathVariable Long id,
                                                           @RequestBody @Valid ProviderRequest request) {
        ProviderResponse response = providerService.updateProvider(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvider(@PathVariable Long id) {
        providerService.deleteProvider(id);
        return ResponseEntity.noContent().build();
    }
}