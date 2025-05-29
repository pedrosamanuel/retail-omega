package com.omega.retail.entity;

import com.omega.retail.enums.InventoryPolicy;
import com.omega.retail.enums.ProductState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String description;
    private Integer currentStock;
    private Integer annualDemand;
    private Double storageCost;
    private Double totalCost;
    private LocalDate deactivationDate;
    @Enumerated(EnumType.STRING)
    private ProductState productState;
    @Enumerated(EnumType.STRING)
    private InventoryPolicy inventoryPolicy;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductProvider> productProviders;
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "fixedLotPolicy_id")
    private FixedLotPolicy fixedLotPolicy;
    @OneToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "fixedIntervalPolicy_id")
    private FixedIntervalPolicy fixedIntervalPolicy;

}

