package com.omega.retail.service;

import com.omega.retail.dto.dtos.FixedIntervalPolicyDTO;
import com.omega.retail.dto.dtos.FixedLotPolicyDTO;
import com.omega.retail.dto.request.ProductRequest;
import com.omega.retail.dto.response.ProductProviderResponse;
import com.omega.retail.dto.response.ProductResponse;
import com.omega.retail.entity.*;
import com.omega.retail.enums.InventoryPolicy;
import com.omega.retail.enums.ProductState;
import com.omega.retail.enums.PurchaseOrderState;
import com.omega.retail.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CalculationService calculationService;
    
    @Autowired
    public ProductService(ProductRepository productRepository, CalculationService calculationService) {
        this.productRepository = productRepository;
        this.calculationService = calculationService;
    }
    
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .code(request.getCode())
                .description(request.getDescription())
                .currentStock(request.getCurrentStock())
                .annualDemand(request.getAnnualDemand())
                .storageCost(request.getStorageCost())
                .productState(ProductState.AlTA)
                .inventoryPolicy(request.getInventoryPolicy())
                .build();

        if(request.getInventoryPolicy() == InventoryPolicy.INTERVALO_FIJO){
            FixedIntervalPolicy fixedIntervalPolicy = new FixedIntervalPolicy();
            fixedIntervalPolicy.setSafetyStock(request.getSafetyStock());
            fixedIntervalPolicy.setReviewIntervalDays(request.getReviewIntervalDays());
            product.setFixedIntervalPolicy(fixedIntervalPolicy);
        }

        if(request.getInventoryPolicy() == InventoryPolicy.LOTE_FIJO){
            FixedLotPolicy fixedLotPolicy = new FixedLotPolicy();
            fixedLotPolicy.setSafetyStock(request.getSafetyStock());
            product.setFixedLotPolicy(fixedLotPolicy);
        }

        product = productRepository.save(product);

        return toResponse(product);
    }
    
    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Product with id "+ id + "not found"));
        return toResponse(product);
    }

    public List<ProductResponse> getProductsBelowSecurityStock() {
        return productRepository.findBelowStockSecurity().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsBelowReorderPointWithoutPendingOrders(){
        return productRepository.findBelowReorderPointWithoutPendingOrders(List.of(PurchaseOrderState.PENDIENTE,PurchaseOrderState.ENVIADA))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }
    public List<ProductResponse> getActiveProductsByProvider(Long providerId) {
        return productRepository.findActiveProductsByProviderId(providerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product with id "+ id + "not found"));
    
        product.setCode(request.getCode());
        product.setDescription(request.getDescription());
        product.setCurrentStock(request.getCurrentStock());
        product.setAnnualDemand(request.getAnnualDemand());
        product.setStorageCost(request.getStorageCost());
        product.setInventoryPolicy(request.getInventoryPolicy());
    
        product = productRepository.save(product);

        calculationService.updateCalculatedFields(product);
    
        return toResponse(product);
    }


    
    public void delete(Long id) {
        if (productRepository.existsByCurrentStock(id)){
            throw new RuntimeException("No se puede eliminar porque el producto tiene stock");
        }
        if (productRepository.existsByActivePurchaseOrder(id, List.of(PurchaseOrderState.PENDIENTE, PurchaseOrderState.ENVIADA))) {
            throw new RuntimeException("No se puede eliminar porque el producto tiene una orden pendiente o enviada");
        }
        Optional<Product> productOptional = productRepository.findById(id);
        if(productOptional.isPresent()){
            Product product = productOptional.get();
            product.setDeactivationDate(LocalDate.now());
            product.setProductState(ProductState.BAJA);
            productRepository.save(product);
        }else {
            throw new EntityNotFoundException("Product with id "+ id + "not found");
        }
    }

    @Transactional
    public void reduceStock(List<SaleDetail> saleDetails) {
        for (SaleDetail detail : saleDetails) {
            Product product = detail.getProduct();

            product.setCurrentStock(product.getCurrentStock() - detail.getQuantity());
            productRepository.save(product);
        }
    }
    @Transactional
    public void increaseStock(List<PurchaseOrderDetail> orderDetails) {
        for (PurchaseOrderDetail detail : orderDetails) {
            Product product = detail.getProduct();

            product.setCurrentStock(product.getCurrentStock() + detail.getQuantity());
            productRepository.save(product);
        }
    }


    private ProductResponse toResponse(Product product) {

        List<ProductProviderResponse> providerResponses = Optional.ofNullable(product.getProductProviders())
                .orElse(Collections.emptyList())
                .stream().map(pp ->
                ProductProviderResponse.builder()
                        .id(pp.getId())
                        .productId(pp.getProduct().getId())
                        .providerId(pp.getProvider().getId())
                        .providerName(pp.getProvider().getName())
                        .unitCost(pp.getUnitCost())
                        .leadTime(pp.getLeadTime())
                        .shippingCost(pp.getShippingCost())
                        .isDefault(pp.getIsDefault())
                        .build()
        ).collect(Collectors.toList());

        FixedLotPolicyDTO lotPolicy = null;
        FixedIntervalPolicyDTO intervalPolicy = null;
    
        if (product.getInventoryPolicy() == InventoryPolicy.LOTE_FIJO) {
            lotPolicy = FixedLotPolicyDTO.builder()
                    .safetyStock(product.getFixedLotPolicy().getSafetyStock())
                    .optimalLotSize(product.getFixedLotPolicy().getOptimalLotSize())
                    .reorderPoint(product.getFixedLotPolicy().getReorderPoint())
                    .build();
        } else if (product.getInventoryPolicy() == InventoryPolicy.INTERVALO_FIJO) {
            intervalPolicy = FixedIntervalPolicyDTO.builder()
                    .lastReviewDate(product.getFixedIntervalPolicy().getLastReviewDate())
                    .maxInventoryLevel(product.getFixedIntervalPolicy().getMaxInventoryLevel())
                    .reviewIntervalDays(product.getFixedIntervalPolicy().getReviewIntervalDays())
                    .safetyStock(product.getFixedIntervalPolicy().getSafetyStock())
                    .build();
        }
    
        return ProductResponse.builder()
                .id(product.getId())
                .code(product.getCode())
                .description(product.getDescription())
                .currentStock(product.getCurrentStock())
                .annualDemand(product.getAnnualDemand())
                .storageCost(product.getStorageCost())
                .totalCost(product.getTotalCost())
                .deactivationDate(product.getDeactivationDate())
                .productState(product.getProductState())
                .inventoryPolicy(product.getInventoryPolicy())
                .fixedLotPolicy(lotPolicy)
                .fixedIntervalPolicy(intervalPolicy)
                .providers(providerResponses)
                .build();
    }

}
