package com.omega.retail.service;

import com.omega.retail.entity.FixedIntervalPolicy;
import com.omega.retail.entity.FixedLotPolicy;
import com.omega.retail.entity.Product;
import com.omega.retail.entity.ProductProvider;
import com.omega.retail.enums.InventoryPolicy;
import com.omega.retail.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalculationService {

    private final ProductRepository productRepository;

    public CalculationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void updateCalculatedFields(Product product) {
        List<ProductProvider> providers = product.getProductProviders();
        if (providers == null || providers.isEmpty()) {
            return;
        }

        ProductProvider defaultProvider = providers.stream()
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

            Float optimalLotSize = (float) Math.sqrt((2*demand*shippingCost)/storageCost);

            product.getFixedLotPolicy().setOptimalLotSize(Math.round(optimalLotSize));
        }

        // === PUNTO DE PEDIDO ===
        if (validateReorderPointFields(product)) {
            // Punto de pedido = d * L + SS
            // donde:
            // d = demanda diaria = D / 365
            // L = lead time
            // SS = stock de seguridad

            Integer leadTime = defaultProvider.getLeadTime();
            Float dailyDemand = demand/365f;
            Integer safetyStock = product.getFixedLotPolicy().getSafetyStock();

            Float reorderPoint = (dailyDemand * leadTime) + safetyStock;

            product.getFixedLotPolicy().setReorderPoint(Math.round(reorderPoint));
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
            Float dailyDemand = demand/365f;
            Integer safetyStock = product.getFixedIntervalPolicy().getSafetyStock();
            Integer reviewIntervalDays = product.getFixedIntervalPolicy().getReviewIntervalDays();

            Float maxInventoryLevel = dailyDemand * (leadTime + reviewIntervalDays) + safetyStock;

            product.getFixedIntervalPolicy().setMaxInventoryLevel(Math.round(maxInventoryLevel));
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
                Float dailyDemand = demand/365f;
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
        if (product.getAnnualDemand() == null ||
                product.getStorageCost() == null ||
                product.getInventoryPolicy() != InventoryPolicy.LOTE_FIJO) {
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
                product.getFixedLotPolicy().getSafetyStock() == null ||
                product.getInventoryPolicy() != InventoryPolicy.LOTE_FIJO) {
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
                product.getFixedIntervalPolicy().getSafetyStock() == null ||
                product.getInventoryPolicy() != InventoryPolicy.INTERVALO_FIJO ) {
            return false;
        }

        ProductProvider defaultProvider = product.getProductProviders().stream()
                .filter(ProductProvider::getIsDefault)
                .findFirst()
                .orElse(null);

        return defaultProvider != null && defaultProvider.getLeadTime() != null;
    }

}
