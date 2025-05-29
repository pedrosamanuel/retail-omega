package com.omega.retail.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Entity
@Data
public class PurchaseOrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Positive
    private Integer quantity;
    private Double price;
    private Double subtotal;

    @ManyToOne
    private Product product;

    @ManyToOne
    private PurchaseOrder purchaseOrder;
}