package com.rental.services.CarRental.product.records;

import lombok.Builder;

import java.time.LocalDate;
@Builder
public record PaymentStatus(int billId, int reservationId, double totalBillAmount, boolean billPaid, int paymentId, double amountPaid, String paymentMethod,
                            LocalDate paymentDate) {
}
