package com.hitechbilling.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ItemDescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long descriptionId;
    private String description;
    private Integer squareFt;
    private Integer quantity;
    private Double rate;
    private Double amount;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonBackReference
    private Invoice invoice;
}
