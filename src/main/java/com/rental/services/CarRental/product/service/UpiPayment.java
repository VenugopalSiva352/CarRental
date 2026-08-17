package com.rental.services.CarRental.product.service;


import com.rental.services.CarRental.product.entity.BillEntity;
import com.rental.services.CarRental.product.entity.PaymentEntity;
import com.rental.services.CarRental.product.entity.VehicleBooking;
import com.rental.services.CarRental.product.enums.PaymentMode;
import com.rental.services.CarRental.product.producer.PaymentNotificationProducer;
import com.rental.services.CarRental.product.records.PaymentStatus;
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
    private PaymentNotificationProducer paymentNotificationProducer;

    @Autowired
    private PaymentRepository paymentRepository;
    @Override
    public void processPayment(int billId, double amount) {
        log.info("Processing UPI payment for bill id: {}", billId);
        log.info("Payment of amount {} received via UPI for bill id: {}", amount, billId);
        log.info("Fetching bill entity for bill id: {}", billId);
        Optional<BillEntity> billEntity = billingRepository.findById(billId);
        if (billEntity.isPresent()) {
            BillEntity bill = billEntity.get();
            PaymentEntity paymentEntity = PaymentEntity.builder().billId(billId).paymentDate(LocalDate.now()).paymentMode(PaymentMode.UPI.toString()).amountPaid(amount).build();
            paymentRepository.save(paymentEntity);
            log.info("Updating payment status for bill id: {}", billId);
            bill.setBillPaid(true);
            billingRepository.save(bill);
            log.info("Payment processed successfully for bill id: {}", billId);
            log.info("Sending payment status to kafka for bill id: {}", billId);
            VehicleBooking vehicleBooking = bill.getReservationId();
            int reservationId = vehicleBooking.getReservationId();
            PaymentStatus paymentStatus = PaymentStatus.builder().billId(billId).reservationId(reservationId).totalBillAmount(bill.getTotalBillAmount()).billPaid(true).paymentId(paymentEntity.getPaymentId()).amountPaid(amount).paymentMethod(PaymentMode.UPI.toString()).paymentDate(LocalDate.now()).build();
            log.info("PaymentStatus before sending to kafka for bill id: {} is : {}", billId,paymentStatus);
            paymentNotificationProducer.sendPaymentStatus("UPI", paymentStatus);
            log.info("Successfully send payment status to kafka for bill id: {}", billId);
        } else {
            log.error("Bill entity not found for bill id: {}", billId);
        }
    }
}
