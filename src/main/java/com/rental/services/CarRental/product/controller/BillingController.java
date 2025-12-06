package com.rental.services.CarRental.product.controller;

import com.rental.services.CarRental.product.dto.Bill;
import com.rental.services.CarRental.product.service.BillingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BillingController {
    @Autowired
    private BillingStrategy billingStrategy;

    @GetMapping("/generateBill")
    public ResponseEntity<String> generateBill(@RequestParam int reservationId) {
        // Implementation for generating bill
        billingStrategy.generateBill(reservationId);
        return ResponseEntity.ok("Bill generated successfully");
    }
    @GetMapping("/bills")
    public ResponseEntity<List<Bill>> getAllBills(@RequestParam int vehicleId) {
        // Implementation for retrieving all bills
        List<Bill> bills = billingStrategy.getAllBills(vehicleId);
        return ResponseEntity.ok(bills);
    }
}
