package com.hitechbilling.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.pl.NIP;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String customerName;
    private String customerAddress;
    private String customerMobile;
    private String termsAndConditions;
    private String note;
    private Double taxableAmount;
    private Double totalAmount;
    private Double receivedAmount;
    private Double pendingAmount;
    private String totalAmountInWords;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemDescription> descriptions;
}
