package com.omega.retail.dto.dtos;

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
public class ProductProviderDTO {
    @NotNull
    private Long providerId;

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
