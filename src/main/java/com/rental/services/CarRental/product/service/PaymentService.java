package com.rental.services.CarRental.product.service;

public interface PaymentService {

    void processPayment(int billId, double amount, String paymentMode);
}
