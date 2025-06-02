package com.omega.retail.dto.response;

import com.omega.retail.enums.PurchaseOrderState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {
    private Long id;
    private LocalDate createdAt;
    private LocalDate sentAt;
    private LocalDate receivedAt;
    private Double total;
    private PurchaseOrderState purchaseOrderState;
    private Long providerId;
    private String providerName;
    @Builder.Default
    private List<PurchaseOrderDetailResponse> details = new ArrayList<>();
}
