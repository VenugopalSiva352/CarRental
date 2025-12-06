package com.rental.services.CarRental.product.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Entity
@Table(name = "payments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int paymentId;
    private int billId;
    private double amountPaid;
    private String paymentMode;
    private LocalDate paymentDate;
}
