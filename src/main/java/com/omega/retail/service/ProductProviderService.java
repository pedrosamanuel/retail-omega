package com.omega.retail.service;


import com.omega.retail.dto.request.ProductProviderRequest;
import com.omega.retail.dto.response.ProductProviderResponse;
import com.omega.retail.entity.Product;
import com.omega.retail.entity.ProductProvider;
import com.omega.retail.entity.Provider;
import com.omega.retail.enums.ProductProviderState;
import com.omega.retail.repository.ProductProviderRepository;
import com.omega.retail.repository.ProductRepository;
import com.omega.retail.repository.ProviderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductProviderService {

    private final ProductProviderRepository productProviderRepository;
    private final ProductRepository productRepository;
    private final ProviderRepository providerRepository;
    private final CalculationService calculationService;

    @Autowired
    public ProductProviderService(ProductProviderRepository productProviderRepository,
                                  ProductRepository productRepository,
                                  ProviderRepository providerRepository, CalculationService calculationService) {
        this.productProviderRepository = productProviderRepository;
        this.productRepository = productRepository;
        this.providerRepository = providerRepository;
        this.calculationService = calculationService;
    }

    public List<ProductProviderResponse> getAll() {
        return productProviderRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ProductProviderResponse getById(Long id) {
        ProductProvider pp = productProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductProvider not found"));
        return toResponse(pp);
    }

    public ProductProviderResponse create(ProductProviderRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Provider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        ProductProvider pp = ProductProvider.builder()
                .product(product)
                .provider(provider)
                .unitCost(request.getUnitCost())
                .leadTime(request.getLeadTime())
                .shippingCost(request.getShippingCost())
                .isDefault(false)
                .deactivationDate(null)
                .productProviderState(ProductProviderState.ALTA)
                .build();

        ProductProvider productProvider = productProviderRepository.save(pp);

        return toResponse(productProvider);
    }

    public ProductProviderResponse update(Long id, ProductProviderRequest request) {
        ProductProvider pp = productProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductProvider not found"));

        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            pp.setProduct(product);
        }

        if (request.getProviderId() != null) {
            Provider provider = providerRepository.findById(request.getProviderId())
                    .orElseThrow(() -> new RuntimeException("Provider not found"));
            pp.setProvider(provider);
        }

        pp.setUnitCost(request.getUnitCost());
        pp.setLeadTime(request.getLeadTime());
        pp.setShippingCost(request.getShippingCost());

        ProductProvider productProvider = productProviderRepository.save(pp);
        calculationService.updateCalculatedFields(pp.getProduct());

        return toResponse(productProvider);
    }

    public boolean delete(Long id) {
        ProductProvider productProvider = productProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductProvider not found"));

        if (Boolean.TRUE.equals(productProvider.getIsDefault())) {
            throw new IllegalStateException("Cannot delete default provider.");
        }

        productProvider.setDeactivationDate(LocalDate.now());
        productProvider.setProductProviderState(ProductProviderState.BAJA);
        productProviderRepository.save(productProvider);
        return true;
    }


    private ProductProviderResponse toResponse(ProductProvider pp) {
        ProductProviderResponse response = new ProductProviderResponse(); 
        response.setId(pp.getId());
        response.setProductId(pp.getProduct().getId());
        response.setProviderId(pp.getProvider().getId());
        response.setProviderName(pp.getProvider().getName());
        response.setUnitCost(pp.getUnitCost());
        response.setLeadTime(pp.getLeadTime());
        response.setShippingCost(pp.getShippingCost());
        response.setIsDefault(pp.getIsDefault());
        return response;
    }

    @Transactional
    public void setDefaultProvider(Long productProviderId) {
        ProductProvider selected = productProviderRepository.findById(productProviderId)
                .orElseThrow(() -> new RuntimeException("ProductProvider not found"));

        Long productId = selected.getProduct().getId();

        productProviderRepository.unsetAllDefaultByProduct(productId);

        productProviderRepository.setDefaultById(productProviderId);

        calculationService.updateCalculatedFields(selected.getProduct());
    }
}
