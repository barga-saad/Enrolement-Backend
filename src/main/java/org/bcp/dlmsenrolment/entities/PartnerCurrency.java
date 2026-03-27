package org.bcp.dlmsenrolment.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "partners_currency")
public class PartnerCurrency {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(length = 3, nullable = false)
        private String currency;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "partner_id")
        private Partner partner;
    }

