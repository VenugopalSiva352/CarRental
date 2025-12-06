package com.rental.services.CarRental.product.controller;

import com.rental.services.CarRental.product.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/processPayment")
    public ResponseEntity<String> processPayment(@RequestParam int billId,
                                                 @RequestParam double amountPaid) {
        paymentService.processPayment(billId, amountPaid);
        return ResponseEntity.ok("Payment processed successfully");
    }
}
