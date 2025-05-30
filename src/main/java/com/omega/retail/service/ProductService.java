package com.omega.retail.service;

import com.omega.retail.dto.dtos.FixedIntervalPolicyDTO;
import com.omega.retail.dto.dtos.FixedLotPolicyDTO;
import com.omega.retail.dto.dtos.ProductProviderDTO;
import com.omega.retail.dto.request.ProductRequest;
import com.omega.retail.dto.response.ProductProviderResponse;
import com.omega.retail.dto.response.ProductResponse;
import com.omega.retail.entity.*;
import com.omega.retail.enums.InventoryPolicy;
import com.omega.retail.enums.ProductState;
import com.omega.retail.enums.PurchaseOrderState;
import com.omega.retail.repository.ProductRepository;
import com.omega.retail.repository.ProviderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProviderRepository providerRepository;
    
    @Autowired
    public ProductService(ProductRepository productRepository, ProviderRepository providerRepository) {
        this.productRepository = productRepository;
        this.providerRepository = providerRepository;
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

        if (request.getProviders() != null) {
            List<ProductProvider> productProviderList = new ArrayList<>();
            for (ProductProviderDTO dto : request.getProviders()) {
                Provider provider = providerRepository.findById(dto.getProviderId())
                        .orElseThrow(() -> new RuntimeException("Provider not found"));
                ProductProvider pp = ProductProvider.builder()
                        .product(product)
                        .provider(provider)
                        .unitCost(dto.getUnitCost())
                        .leadTime(dto.getLeadTime())
                        .shippingCost(dto.getShippingCost())
                        .build();
                productProviderList.add(pp);
            }
            product.setProductProviders(productProviderList);
        }

        product = productRepository.save(product);

        return toResponse(product);
    }
    
    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        return toResponse(product);
    }
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    
        product.setCode(request.getCode());
        product.setDescription(request.getDescription());
        product.setCurrentStock(request.getCurrentStock());
        product.setAnnualDemand(request.getAnnualDemand());
        product.setStorageCost(request.getStorageCost());
        product.setInventoryPolicy(request.getInventoryPolicy());
    
        product = productRepository.save(product);

    
        if (request.getProviders() != null) {
            List<ProductProvider> productProviders = new ArrayList<>();
            for (ProductProviderDTO dto : request.getProviders()) {
                Provider provider = providerRepository.findById(dto.getProviderId())
                        .orElseThrow(() -> new RuntimeException("Provider not found"));
                ProductProvider pp = ProductProvider.builder()
                        .product(product)
                        .provider(provider)
                        .unitCost(dto.getUnitCost())
                        .leadTime(dto.getLeadTime())
                        .shippingCost(dto.getShippingCost())
                        .build();
                productProviders.add(pp);
            }
            product.setProductProviders(productProviders);
        }
        updateCalculatedFields(product);
        product = productRepository.save(product);
    
        return toResponse(product);
    }
    
    public void delete(Long id) {
        if (productRepository.existsByCurrentStock(id)){
            throw new RuntimeException("Product has current stock");
        }
        if (productRepository.existsByActivePurchaseOrder(id, List.of(PurchaseOrderState.PENDIENTE, PurchaseOrderState.ENVIADA))) {
            throw new RuntimeException("Product is associated with a sent or pending purchase order");
        }
        Optional<Product> productOptional = productRepository.findById(id);
        if(productOptional.isPresent()){
            Product product = productOptional.get();
            product.setDeactivationDate(LocalDate.now());
            product.setProductState(ProductState.BAJA);
            productRepository.save(product);
        }else {
            throw new RuntimeException("Product not found");
        }
    }

    @Transactional
    public void setDefaultProvider(Long productId, Long providerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        productRepository.unsetAllDefaultByProduct(productId);
        productRepository.setDefaultById(productId, providerId);
        updateCalculatedFields(product);
    }

    public List<ProductResponse> getProductsBelowSecurityStock() {
        return productRepository.findBelowStockSecurity().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsBelowReorderPointWithoutPendingOrders(){
        return productRepository.findBelowReorderPointWithoutPendingOrders(List.of(PurchaseOrderState.PENDIENTE,PurchaseOrderState.ENVIADA))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ProductResponse toResponse(Product product) {
    
        List<ProductProviderResponse> providerResponses = product.getProductProviders().stream().map(pp ->
                ProductProviderResponse.builder()
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

    public void updateCalculatedFields(Product product) {
        ProductProvider defaultProvider = product.getProductProviders().stream()
                .filter(ProductProvider::getIsDefault)
                .findFirst()
                .orElse(null);

        Integer demand = product.getAnnualDemand();
        Double storageCost = product.getStorageCost();

        // === LOTE ÓPTIMO ===
        if (validateOptimalLotSizeFields(product)) {
            // Lote óptimo = sqrt((2 * D * S) / H)
            // donde:
            // D = demanda anual
            // S = costo de envío
            // H = costo de almacenamiento

            Double shippingCost = defaultProvider.getShippingCost();

            Integer optimalLotSize = (int) Math.sqrt((2*demand*shippingCost)/storageCost);

            product.getFixedLotPolicy().setOptimalLotSize(optimalLotSize);
        }

        // === PUNTO DE PEDIDO ===
        if (validateReorderPointFields(product)) {
            // Punto de pedido = d * L + SS
            // donde:
            // d = demanda diaria = D / 365
            // L = lead time
            // SS = stock de seguridad

            Integer leadTime = defaultProvider.getLeadTime();
            Integer dailyDemand = demand/365;
            Integer safetyStock = product.getFixedLotPolicy().getSafetyStock();

            Integer reorderPoint = dailyDemand * leadTime + safetyStock;

            product.getFixedLotPolicy().setReorderPoint(reorderPoint);
        }

        // === INVENTARIO MÁXIMO ===
        if (validateMaxInventoryLevelFields(product)) {
            // Inventario máximo = d * (T + L) + SS
            // donde:
            // d = demanda diaria = D / 365
            // T = intervalo de revisión (en días)
            // L = lead time
            // SS = stock de seguridad

            Integer leadTime = defaultProvider.getLeadTime();
            Integer dailyDemand = demand/365;
            Integer safetyStock = product.getFixedIntervalPolicy().getSafetyStock();
            Integer reviewIntervalDays = product.getFixedIntervalPolicy().getReviewIntervalDays();

            Integer maxInventoryLevel = dailyDemand * (leadTime + reviewIntervalDays) + safetyStock;

            product.getFixedIntervalPolicy().setMaxInventoryLevel(maxInventoryLevel);
        }

        // === COSTO TOTAL ===
        if (validateTotalCostFields(product)) {
            if (product.getInventoryPolicy() == InventoryPolicy.LOTE_FIJO) {
                // Costo total = (D / Q) * S + (Q / 2) * H + D * C
                // donde:
                // D = demanda anual
                // Q = tamaño de lote óptimo
                // S = costo de envío
                // H = costo de almacenamiento anual por unidad
                // C = costo unitario

                Double unitCost =  defaultProvider.getUnitCost();
                Integer optimalLot = product.getFixedLotPolicy().getOptimalLotSize();
                Double shippingCost = defaultProvider.getShippingCost();

                Double totalCost = (demand/optimalLot)*shippingCost + (optimalLot/2)*storageCost + demand*unitCost;

                product.setTotalCost(totalCost);
            } else if (product.getInventoryPolicy() == InventoryPolicy.INTERVALO_FIJO) {
                // Costo total = D * C + (D / Q) * S + ((Imax - d * L) / 2) * H
                // donde:
                // D = demanda anual
                // Q = cantidad pedida por vez = d * T
                // S = costo de envío
                // Imax = inventario máximo
                // d = demanda diaria = D / 365
                // L = lead time
                // H = costo de almacenamiento
                // C = costo unitario

                Double unitCost =  defaultProvider.getUnitCost();
                Double shippingCost = defaultProvider.getShippingCost();
                Integer dailyDemand = demand/365;
                Integer leadTime = defaultProvider.getLeadTime();
                Integer maxInventoryLevel = product.getFixedIntervalPolicy().getMaxInventoryLevel();
                Integer reviewIntervalDays = product.getFixedIntervalPolicy().getReviewIntervalDays();

                Double totalCost = demand*unitCost + (demand/(dailyDemand*reviewIntervalDays))*shippingCost + ((maxInventoryLevel-dailyDemand*leadTime)/2)*storageCost;


                product.setTotalCost(totalCost);
            }
        }
        productRepository.save(product);
    }

    public boolean validateTotalCostFields(Product product) {
        if (product.getAnnualDemand() == null || product.getStorageCost() == null) {
            return false;
        }

        ProductProvider defaultProvider = product.getProductProviders().stream()
                .filter(ProductProvider::getIsDefault)
                .findFirst()
                .orElse(null);

        if (defaultProvider == null ||
                defaultProvider.getShippingCost() == null ||
                defaultProvider.getUnitCost() == null) {
            return false;
        }

        switch (product.getInventoryPolicy()) {
            case LOTE_FIJO:
                FixedLotPolicy lotPolicy = product.getFixedLotPolicy();
                return lotPolicy != null && lotPolicy.getOptimalLotSize() != null;

            case INTERVALO_FIJO:
                FixedIntervalPolicy intervalPolicy = product.getFixedIntervalPolicy();
                return intervalPolicy != null &&
                        intervalPolicy.getReviewIntervalDays() != null &&
                        intervalPolicy.getMaxInventoryLevel() != null &&
                        defaultProvider.getLeadTime() != null;


            default:
                return false;
        }
    }
    public boolean validateOptimalLotSizeFields(Product product) {
        if (product.getAnnualDemand() == null || product.getStorageCost() == null) {
            return false;
        }

        ProductProvider defaultProvider = product.getProductProviders().stream()
                .filter(ProductProvider::getIsDefault)
                .findFirst()
                .orElse(null);

        return defaultProvider != null && defaultProvider.getShippingCost() != null;
    }
    public boolean validateReorderPointFields(Product product) {
        if (product.getAnnualDemand() == null ||
                product.getFixedLotPolicy() == null ||
                product.getFixedLotPolicy().getSafetyStock() == null) {
            return false;
        }

        ProductProvider defaultProvider = product.getProductProviders().stream()
                .filter(ProductProvider::getIsDefault)
                .findFirst()
                .orElse(null);

        return defaultProvider != null && defaultProvider.getLeadTime() != null;
    }

    public boolean validateMaxInventoryLevelFields(Product product) {
        if (product.getAnnualDemand() == null ||
                product.getFixedIntervalPolicy() == null ||
                product.getFixedIntervalPolicy().getReviewIntervalDays() == null ||
                product.getFixedIntervalPolicy().getSafetyStock() == null) {
            return false;
        }

        ProductProvider defaultProvider = product.getProductProviders().stream()
                .filter(ProductProvider::getIsDefault)
                .findFirst()
                .orElse(null);

        return defaultProvider != null && defaultProvider.getLeadTime() != null;
    }


}
