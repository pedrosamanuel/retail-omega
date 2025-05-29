package com.omega.retail.dto.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FixedIntervalPolicyDTO {
    private Integer safetyStock;     // stockSeguridad
    private Integer reviewIntervalDays;  // intervaloRevision
    private Integer maxInventoryLevel;   // inventarioMaximo
    private LocalDate lastReviewDate;    // fechaUltimaRevision
}
