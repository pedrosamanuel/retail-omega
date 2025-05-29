package com.omega.retail.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Entity
@Data
public class FixedLotPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer optimalLotSize;      // loteOptimo
    private Integer reorderPoint;        // puntoPedido
    @NotNull
    @PositiveOrZero
    private Integer safetyStock;         // stockSeguridad

}
