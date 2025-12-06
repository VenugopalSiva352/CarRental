package com.rental.services.CarRental.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PaymentDTO {
    private int paymentId;
    private int billId;
    private double amountPaid;
    private String paymentMode;
    private LocalDate paymentDate;
}
