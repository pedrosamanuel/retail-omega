package com.omega.retail.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
public class SaleDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Positive
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;

    @ManyToOne
    private Product product;

    @ManyToOne
    private Sale sale;
}