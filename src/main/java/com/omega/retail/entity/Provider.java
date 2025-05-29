package com.omega.retail.entity;

import com.omega.retail.enums.ProviderState;
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
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String phone;
    @Enumerated(EnumType.STRING)
    private ProviderState providerState;
    private LocalDate deactivateDate;

    @OneToMany(mappedBy = "provider")
    private List<PurchaseOrder> purchaseOrders;

    @OneToMany(mappedBy = "provider")
    private List<ProductProvider> products;
}
