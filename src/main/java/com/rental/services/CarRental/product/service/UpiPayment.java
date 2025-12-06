package com.rental.services.CarRental.product.service;


import com.rental.services.CarRental.product.entity.BillEntity;
import com.rental.services.CarRental.product.entity.PaymentEntity;
import com.rental.services.CarRental.product.repositories.BillingRepository;
import com.rental.services.CarRental.product.repositories.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
@Service
@Slf4j
public class UpiPayment implements PaymentService {

    @Autowired
    private BillingRepository billingRepository;

    @Autowired
    private PaymentRepository paymentRepository;
    @Override
    public void processPayment(int billId, double amount, String paymentMode) {
        log.info("Processing UPI payment for bill id: {}", billId);
        log.info("Payment of amount {} received via UPI for bill id: {}", amount, billId);
        log.info("Fetching bill entity for bill id: {}", billId);
        Optional<BillEntity> billEntity = billingRepository.findById(billId);
        if (billEntity.isPresent()) {
            BillEntity bill = billEntity.get();
            PaymentEntity paymentEntity = PaymentEntity.builder().billId(billId).paymentDate(LocalDate.now()).paymentMode(paymentMode).amountPaid(amount).build();
            paymentRepository.save(paymentEntity);
            log.info("Updating payment status for bill id: {}", billId);
            bill.setBillPaid(true);
            billingRepository.save(bill);
            log.info("Payment processed successfully for bill id: {}", billId);
        } else {
            log.error("Bill entity not found for bill id: {}", billId);
        }
    }
}
