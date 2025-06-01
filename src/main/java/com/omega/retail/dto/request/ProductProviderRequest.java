package com.omega.retail.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductProviderRequest {
    @NotNull
    private Long providerId;
    @NotNull
    private Long productId;
    @NotNull
    @Positive
    private Double unitCost;

    @NotNull
    @Positive
    private Integer leadTime;

    @NotNull
    @Positive
    private Double shippingCost;
}
