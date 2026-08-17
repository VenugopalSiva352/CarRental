package com.rental.services.CarRental.product.controller;

import com.rental.services.CarRental.product.producer.PaymentNotificationProducer;
import com.rental.services.CarRental.product.records.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/kafka")
public class KafkaTestController {

    private final PaymentNotificationProducer paymentNotificationProducer;

    public KafkaTestController(PaymentNotificationProducer paymentNotificationProducer) {
        this.paymentNotificationProducer = paymentNotificationProducer;
    }

    /**
     * Test endpoint to publish a payment status message to Kafka
     * The message will be consumed by PaymentStatusConsumer and saved to database
     */
    @PostMapping("/test-payment-message")
    public ResponseEntity<Map<String, Object>> testPaymentMessage(
            @RequestParam(defaultValue = "1") int paymentId,
            @RequestParam(defaultValue = "1") int billId,
            @RequestParam(defaultValue = "1") int reservationId,
            @RequestParam(defaultValue = "5000") double totalBillAmount,
            @RequestParam(defaultValue = "5000") double amountPaid,
            @RequestParam(defaultValue = "true") boolean billPaid,
            @RequestParam(defaultValue = "CREDIT_CARD") String paymentMethod
    ) {
        log.info("Received request to test payment message");

        try {
            // Create a payment status record
            PaymentStatus paymentStatus = PaymentStatus.builder()
                    .paymentId(paymentId)
                    .billId(billId)
                    .reservationId(reservationId)
                    .totalBillAmount(totalBillAmount)
                    .amountPaid(amountPaid)
                    .billPaid(billPaid)
                    .paymentMethod(paymentMethod)
                    .paymentDate(LocalDate.now())
                    .build();

            log.info("Publishing test payment message to Kafka...");
            // Publish to Kafka
            paymentNotificationProducer.sendPaymentStatus(String.valueOf(paymentId), paymentStatus);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Payment message published to Kafka topic: rental-payment-topic");
            response.put("paymentId", paymentId);
            response.put("billId", billId);
            response.put("totalBillAmount", totalBillAmount);
            response.put("amountPaid", amountPaid);
            response.put("timestamp", System.currentTimeMillis());

            log.info("Response: {}", response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error publishing payment message", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Failed to publish payment message: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "CarRental Kafka Integration");
        return ResponseEntity.ok(response);
    }
}

