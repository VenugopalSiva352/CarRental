package com.rental.services.CarRental.product.service;

import com.rental.services.CarRental.product.entity.PaymentStatusEntity;
import com.rental.services.CarRental.product.records.PaymentStatus;
import com.rental.services.CarRental.product.repositories.PaymentStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class PaymentStatusConsumer {

    private final PaymentStatusRepository paymentStatusRepository;

    public PaymentStatusConsumer(PaymentStatusRepository paymentStatusRepository) {
        this.paymentStatusRepository = paymentStatusRepository;
    }

    /**
     * Kafka Listener - Receives messages from rental-payment-topic
     * Delegates processing to processPaymentStatus() method
     */
    @KafkaListener(topics = "rental-payment-topic", groupId = "car-rental-group")
    public void consumePaymentStatus(
            @Payload PaymentStatus paymentStatus,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(name = "kafka_receivedPartitionId") int partition,
            @Header(name = "kafka_offset") long offset
    ) {
        log.info("Message received on Kafka listener - delegating to service for processing...");
        // Process the received message using the service method
        processPaymentStatus(paymentStatus, topic, partition, offset);
    }

    /**
     * Service method to process received PaymentStatus message
     * This method handles the actual business logic:
     * - Logs the received message details
     * - Creates and saves entity to database
     * - Logs the result (success or error)
     *
     * Can be called from:
     * 1. Kafka listener (consumePaymentStatus)
     * 2. Other services or controllers
     * 3. Unit tests
     *
     * @param paymentStatus The payment status received from Kafka listener
     * @param topic The Kafka topic name
     * @param partition The Kafka partition
     * @param offset The message offset
     */
    public void processPaymentStatus(
            PaymentStatus paymentStatus,
            String topic,
            int partition,
            long offset
    ) {
        log.info("======================================");
        log.info("Processing received Kafka message");
        log.info("Topic: {}", topic);
        log.info("Partition: {}, Offset: {}", partition, offset);
        log.info("Payment Status Details:");
        log.info("  - Payment ID: {}", paymentStatus.paymentId());
        log.info("  - Bill ID: {}", paymentStatus.billId());
        log.info("  - Reservation ID: {}", paymentStatus.reservationId());
        log.info("  - Total Bill Amount: {}", paymentStatus.totalBillAmount());
        log.info("  - Amount Paid: {}", paymentStatus.amountPaid());
        log.info("  - Bill Paid: {}", paymentStatus.billPaid());
        log.info("  - Payment Method: {}", paymentStatus.paymentMethod());
        log.info("  - Payment Date: {}", paymentStatus.paymentDate());
        log.info("======================================");

        try {
            // Create entity from received payment status
            PaymentStatusEntity entity = convertToEntity(paymentStatus);

            // Save to database
            PaymentStatusEntity savedEntity = paymentStatusRepository.save(entity);

            // Log success
            logSuccess(savedEntity, topic, partition, offset);

        } catch (Exception e) {
            // Log error
            logError(paymentStatus, e);
            // Rethrow to trigger error handler and allow retry
            throw new RuntimeException("Failed to save payment status to database", e);
        }
    }

    /**
     * Convert PaymentStatus record to PaymentStatusEntity
     * This is used by the service method to prepare the entity for persistence
     *
     * @param paymentStatus The payment status received from Kafka
     * @return PaymentStatusEntity ready for database save
     */
    private PaymentStatusEntity convertToEntity(PaymentStatus paymentStatus) {
        return PaymentStatusEntity.builder()
                .paymentId(paymentStatus.paymentId())
                .billId(paymentStatus.billId())
                .reservationId(paymentStatus.reservationId())
                .totalBillAmount(paymentStatus.totalBillAmount())
                .amountPaid(paymentStatus.amountPaid())
                .billPaid(paymentStatus.billPaid())
                .paymentMethod(paymentStatus.paymentMethod())
                .paymentDate(paymentStatus.paymentDate())
                .kafkaMessageReceivedAt(LocalDateTime.now())
                .savedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Log successful database save
     */
    private void logSuccess(PaymentStatusEntity savedEntity, String topic, int partition, long offset) {
        log.info("✓ Payment status successfully saved to database");
        log.info("  - Topic: {}, Partition: {}, Offset: {}", topic, partition, offset);
        log.info("  - Entity ID: {}", savedEntity.getId());
        log.info("  - Payment ID: {}", savedEntity.getPaymentId());
        log.info("  - Saved at: {}", savedEntity.getSavedAt());
        log.info("✓ Kafka message processing completed successfully");
    }

    /**
     * Log error during processing
     */
    private void logError(PaymentStatus paymentStatus, Exception e) {
        log.error("✗ Error saving payment status to database");
        log.error("  - Payment ID: {}", paymentStatus.paymentId());
        log.error("  - Bill ID: {}", paymentStatus.billId());
        log.error("  - Error message: {}", e.getMessage());
        log.error("  - Exception: ", e);
    }
}

