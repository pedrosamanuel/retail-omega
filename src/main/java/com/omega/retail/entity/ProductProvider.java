package com.omega.retail.entity;

import com.omega.retail.enums.ProductProviderState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double unitCost;
    private Integer leadTime;
    private Double shippingCost;
    private Boolean isDefault;
    @Enumerated(EnumType.STRING)
    private ProductProviderState productProviderState;
    private LocalDate deactivationDate;
    @ManyToOne
    private Product product;

    @ManyToOne
    private Provider provider;

}
