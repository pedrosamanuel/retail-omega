package com.omega.retail.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class FixedIntervalPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @PositiveOrZero
    private Integer safetyStock;     // stockSeguridad
    @NotNull
    @Positive
    private Integer reviewIntervalDays;  // intervaloRevision
    private Integer maxInventoryLevel;   // inventarioMaximo
    private LocalDate lastReviewDate;    // fechaUltimaRevision

}
