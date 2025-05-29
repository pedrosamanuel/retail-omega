package com.omega.retail.dto.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FixedLotPolicyDTO {
    private Integer optimalLotSize;
    private Integer reorderPoint;
    private Integer safetyStock;
}
