package com.omega.retail.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderRequest {
    @NotNull
    private Long providerId;
    @Builder.Default
    @NotNull
    private List<PurchaseOrderDetailRequest> details = new ArrayList<>();
}
