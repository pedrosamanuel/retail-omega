package com.omega.retail.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleDetailRequest {
    @NotNull
    @Positive
    private Integer quantity;
    @NotNull
    @Positive
    private Double unitPrice;
    @NotNull
    private Long productId;
}
