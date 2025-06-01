package com.omega.retail.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductProviderResponse {
    private Long id;
    private Long providerId;
    private Long productId;
    private String providerName;
    private Double unitCost;
    private Integer leadTime;
    private Double shippingCost;
    private Boolean isDefault;
}
