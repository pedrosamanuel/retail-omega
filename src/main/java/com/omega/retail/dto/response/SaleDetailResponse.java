package com.omega.retail.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleDetailResponse {
    private Long id;
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;

    private Long productId;
    private String productCode;
    private String productDescription;
}
