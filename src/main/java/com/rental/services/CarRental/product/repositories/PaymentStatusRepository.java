package com.rental.services.CarRental.product.repositories;

import com.rental.services.CarRental.product.entity.PaymentStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentStatusRepository extends JpaRepository<PaymentStatusEntity, Long> {

    /**
     * Find all payment status logs for a specific payment ID
     */
    List<PaymentStatusEntity> findByPaymentId(int paymentId);

    /**
     * Find all payment status logs for a specific bill ID
     */
    List<PaymentStatusEntity> findByBillId(int billId);

    /**
     * Find all payment status logs saved after a specific timestamp
     */
    List<PaymentStatusEntity> findBySavedAtAfter(LocalDateTime savedAt);
}

