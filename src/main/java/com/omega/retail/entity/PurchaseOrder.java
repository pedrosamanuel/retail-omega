package com.omega.retail.entity;

import com.omega.retail.enums.PurchaseOrderState;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate createdAt;
    private LocalDate sentAt;
    private LocalDate receivedAt;
    @Positive
    private Double total;
    private PurchaseOrderState purchaseOrderState;
    @OneToMany(mappedBy = "purchaseOrder")
    private List<PurchaseOrderDetail> details = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;
}