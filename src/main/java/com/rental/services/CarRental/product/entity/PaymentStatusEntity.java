package com.rental.services.CarRental.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_status_logs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "payment_id")
    private int paymentId;

    @Column(name = "bill_id")
    private int billId;

    @Column(name = "reservation_id")
    private int reservationId;

    @Column(name = "total_bill_amount")
    private double totalBillAmount;

    @Column(name = "amount_paid")
    private double amountPaid;

    @Column(name = "bill_paid")
    private boolean billPaid;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "kafka_message_received_at")
    private LocalDateTime kafkaMessageReceivedAt;

    @Column(name = "saved_at")
    private LocalDateTime savedAt;
}

