package com.rental.services.CarRental.product.producer;

import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
import com.rental.services.CarRental.product.records.PaymentStatus;

import java.util.concurrent.CompletableFuture;

@Service
public class PaymentNotificationProducer {

    private static final String TOPIC = "rental-payment-topic";

    private final KafkaTemplate<String, PaymentStatus> kafkaTemplate;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentNotificationProducer.class);

    public PaymentNotificationProducer(KafkaTemplate<String, PaymentStatus> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentStatus(String paymentId, PaymentStatus status) {
        log.info("Publishing payment status for id={} status={}", paymentId, status);
        CompletableFuture<SendResult<String, PaymentStatus>> sendResultCompletableFuture = kafkaTemplate.send(TOPIC, paymentId, status);
        sendResultCompletableFuture.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish Kafka message", ex);
                return;
            }

            log.info(
                    "Kafka message published successfully. Topic={}, Partition={}, Offset={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
    }

}
