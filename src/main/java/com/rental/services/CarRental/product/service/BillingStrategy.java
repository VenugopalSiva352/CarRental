package com.rental.services.CarRental.product.service;

import com.rental.services.CarRental.product.dto.Bill;

import java.util.List;

public interface BillingStrategy {
    Bill generateBill(int bookingId);

    List<Bill> getAllBills(int vehicleId);
}
