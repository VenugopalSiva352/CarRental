package com.rental.services.CarRental.product.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class Bill {
    private int billId;
    private int reservationId;
    private double totalBillAmount;
    private boolean billPaid;
}
