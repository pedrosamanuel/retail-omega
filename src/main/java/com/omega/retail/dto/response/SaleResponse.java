package com.omega.retail.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {
    private Long id;
    private Double total;
    private LocalDateTime date;
    @Builder.Default
    private List<SaleDetailResponse> saleDetailResponses = new ArrayList<>();
}
