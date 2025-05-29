package com.omega.retail.dto.response;

import com.omega.retail.enums.ProviderState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProviderResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private ProviderState providerState;
    private LocalDate deactivateDate;
}
