package com.rental.services.CarRental.product.service;

import com.rental.services.CarRental.product.records.PaymentStatus;

public interface PaymentService {

    void processPayment(int billId, double amount);

    default void sendPaymentStatusToKafka(int billId, PaymentStatus paymentStatus) {
    }

}
