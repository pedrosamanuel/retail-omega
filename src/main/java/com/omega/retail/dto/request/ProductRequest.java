package com.omega.retail.dto.request;

import com.omega.retail.dto.dtos.ProductProviderDTO;
import com.omega.retail.enums.InventoryPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    @NotNull
    @NotBlank
    private String code;
    @NotNull
    @NotBlank
    private String description;
    @NotNull
    @PositiveOrZero
    private Integer currentStock;
    @NotNull
    @Positive
    private Integer annualDemand;
    @NotNull
    @Positive
    private Double storageCost;
    @NotNull
    private InventoryPolicy inventoryPolicy;
    @PositiveOrZero
    private Integer safetyStock;
    @Positive
    private Integer reviewIntervalDays;

    private List<ProductProviderDTO> providers;

}
