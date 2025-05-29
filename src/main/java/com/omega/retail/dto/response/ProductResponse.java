package com.omega.retail.dto.response;

import com.omega.retail.dto.dtos.FixedIntervalPolicyDTO;
import com.omega.retail.dto.dtos.FixedLotPolicyDTO;
import com.omega.retail.enums.InventoryPolicy;
import com.omega.retail.enums.ProductState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String code;
    private String description;
    private Integer currentStock;
    private Integer annualDemand;
    private Double storageCost;
    private Double totalCost;
    private LocalDate deactivationDate;
    private ProductState productState;
    private InventoryPolicy inventoryPolicy;

    private FixedLotPolicyDTO fixedLotPolicy;
    private FixedIntervalPolicyDTO fixedIntervalPolicy;

    private List<ProductProviderResponse> providers;
}
