package com.omega.retail.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    @Positive
    private Double total;

    @OneToMany(mappedBy = "sale")
    private List<SaleDetail> details = new ArrayList<>();
}
