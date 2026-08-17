# Kafka Integration - Car Rental Application

## Overview

This document describes the Kafka integration implemented in the Car Rental application. The system publishes payment status events to Kafka and persists them to the database through a consumer listener.

## Architecture

### Components

1. **PaymentNotificationProducer** (`PaymentNotificationProducer.java`)
   - Publishes payment status messages to the `rental-payment-topic`
   - Logs the result including topic, partition, and offset
   - Uses `KafkaTemplate<String, PaymentStatus>`

2. **PaymentStatusConsumer** (`PaymentStatusConsumer.java`)
   - Listens to the `rental-payment-topic`
   - Consumes `PaymentStatus` records
   - Saves received data to the `payment_status_logs` table
   - Logs all published results with detailed metadata

3. **PaymentStatusEntity** (`PaymentStatusEntity.java`)
   - JPA entity representing payment status logs
   - Stores payment data received from Kafka
   - Includes timestamps for when message was received and saved

4. **PaymentStatusRepository** (`PaymentStatusRepository.java`)
   - JPA repository for database operations
   - Provides query methods for filtering payment status logs

5. **KafkaTestController** (`KafkaTestController.java`)
   - Test endpoint to publish sample payment messages
   - Useful for testing the end-to-end flow

## Database Schema

The `payment_status_logs` table stores received Kafka messages:

```sql
CREATE TABLE payment_status_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id INT NOT NULL,
    bill_id INT NOT NULL,
    reservation_id INT NOT NULL,
    total_bill_amount DOUBLE NOT NULL,
    amount_paid DOUBLE NOT NULL,
    bill_paid BOOLEAN NOT NULL,
    payment_method VARCHAR(50),
    payment_date DATE,
    kafka_message_received_at TIMESTAMP,
    saved_at TIMESTAMP
);
```

## Setup Instructions

### Prerequisites

- Docker and Docker Compose installed
- Java 17 or higher
- Gradle build tool

### Step 1: Start Kafka, Zookeeper, and MySQL

```bash
cd /Users/gopal/Documents/STS3_ARM_ARC/CarRental

# Start all services
docker-compose up -d

# Verify services are running
docker-compose ps
```

Expected output:
```
CONTAINER ID   IMAGE                           STATUS      PORTS
xxx            confluentinc/cp-kafka:7.5.0     Up 2 mins   0.0.0.0:9092->9092/tcp
xxx            confluentinc/cp-zookeeper:7.5.0 Up 2 mins   0.0.0.0:2181->2181/tcp
xxx            mysql:8.0                       Up 2 mins   0.0.0.0:3306->3306/tcp
```

### Step 2: Build the Application

```bash
cd /Users/gopal/Documents/STS3_ARM_ARC/CarRental
./gradlew clean build -x test
```

### Step 3: Run the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## Testing the Implementation

### Method 1: Using the Test Endpoint

Publish a test payment message via HTTP:

```bash
# Basic test with default values
curl -X POST "http://localhost:8080/api/kafka/test-payment-message" \
  -H "Content-Type: application/json"

# Test with custom parameters
curl -X POST "http://localhost:8080/api/kafka/test-payment-message?paymentId=123&billId=456&totalBillAmount=15000&amountPaid=15000&paymentMethod=UPI" \
  -H "Content-Type: application/json"
```

Expected Response:
```json
{
  "status": "SUCCESS",
  "message": "Payment message published to Kafka topic: rental-payment-topic",
  "paymentId": 123,
  "billId": 456,
  "totalBillAmount": 15000,
  "amountPaid": 15000,
  "timestamp": 1692080123456
}
```

### Method 2: Using the Application API

Integrate the test endpoint into your workflow or use it programmatically.

## Monitoring and Logging

### Application Logs

The application logs are configured to show:

1. **Producer Logging** (PaymentNotificationProducer):
   ```
   Publishing payment status for id=123 status=PaymentStatus(...)
   Kafka message published successfully. Topic=rental-payment-topic, Partition=0, Offset=5
   ```

2. **Consumer Logging** (PaymentStatusConsumer):
   ```
   ======================================
   Kafka Message Received from Topic: rental-payment-topic
   Partition: 0, Offset: 5
   Payment Status Details:
     - Payment ID: 123
     - Bill ID: 456
     ...
   ======================================
   ✓ Payment status successfully saved to database
     - Entity ID: 1
     - Saved at: 2024-01-15 10:30:45.123
   ✓ Kafka message processing completed successfully
   ```

3. **Error Logging**:
   ```
   ✗ Error saving payment status to database
     - Payment ID: 123
     - Error message: Connection refused
   ```

### View Application Logs

```bash
# Follow logs in real-time
docker logs -f car-rental-app

# View Kafka broker logs
docker logs -f kafka

# View Zookeeper logs
docker logs -f zookeeper
```

### Verify Data in Database

```bash
# Connect to MySQL
mysql -h localhost -u root -proot@root CarRental

# Query payment status logs
SELECT * FROM payment_status_logs ORDER BY saved_at DESC LIMIT 10;

# Count total messages saved
SELECT COUNT(*) as total_messages FROM payment_status_logs;

# Get statistics by payment method
SELECT payment_method, COUNT(*) as count, SUM(amount_paid) as total_paid 
FROM payment_status_logs 
GROUP BY payment_method;
```

## Kafka Configuration

The Kafka configuration in `application.yaml`:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all  # Wait for all replicas to acknowledge
    consumer:
      group-id: car-rental-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest  # Start from beginning if no offset
      max-poll-records: 1  # Process one message at a time
      properties:
        spring.json.trusted.packages: "*"
    listener:
      ack-mode: MANUAL_IMMEDIATE  # Acknowledge immediately after processing
      poll-timeout: 3000
      concurrency: 1  # Single thread consumer for ordered processing
```

## Log Levels

To control logging verbosity, modify `application.yaml`:

```yaml
logging:
  level:
    root: INFO
    com.rental.services.CarRental: DEBUG  # Application debug logs
    org.springframework.kafka: INFO        # Spring Kafka framework
    org.apache.kafka: WARN                 # Apache Kafka client (only warnings)
```

## Troubleshooting

### 1. Kafka Connection Failed

```
Error: Connection refused [localhost:9092]
```

**Solution:**
```bash
# Verify Kafka is running
docker-compose ps

# Check Kafka logs
docker logs kafka

# Restart Kafka
docker-compose restart kafka
```

### 2. Consumer Not Receiving Messages

```
No messages being consumed
```

**Solution:**
- Check if the topic exists: `docker exec kafka kafka-topics --list --bootstrap-server localhost:9092`
- Create topic if missing: `docker exec kafka kafka-topics --create --topic rental-payment-topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1`
- Verify consumer group: `docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list`

### 3. Database Save Failures

```
✗ Error saving payment status to database
```

**Solution:**
- Verify MySQL is running: `docker-compose ps`
- Check MySQL logs: `docker logs mysql_db`
- Verify database and table exist:
  ```bash
  mysql -h localhost -u root -proot@root CarRental -e "SHOW TABLES;"
  ```

### 4. JSON Deserialization Issues

```
Error deserializing record value for partition rental-payment-topic-0
```

**Solution:**
- Ensure `spring.json.trusted.packages: "*"` is set in consumer config
- Verify PaymentStatus record structure hasn't changed
- Clear Kafka offset and restart: `docker-compose down && docker-compose up -d`

## Performance Tuning

For production use, consider these configurations:

```yaml
spring:
  kafka:
    consumer:
      max-poll-records: 50  # Batch process for better throughput
      fetch-min-bytes: 1024  # Minimum bytes to fetch
      fetch-max-wait-ms: 500  # Wait up to 500ms
    listener:
      concurrency: 3  # Multiple consumers in parallel
      poll-timeout: 10000  # Longer timeout for processing
```

## Cleanup

To remove containers and volumes:

```bash
# Stop and remove containers
docker-compose down

# Remove volumes (persistent data)
docker-compose down -v
```

## Next Steps

1. **Error Handling**: Add retry logic and Dead Letter Queue (DLQ) for failed messages
2. **Metrics**: Add Spring Boot Actuator for Kafka metrics and monitoring
3. **Schema Registry**: Implement Avro schema for better data validation
4. **Multi-partition**: Scale to multiple partitions for higher throughput
5. **Security**: Enable Kafka authentication and encryption for production

## Files Modified/Created

- ✅ `PaymentStatusEntity.java` - New entity for persistence
- ✅ `PaymentStatusRepository.java` - New repository interface
- ✅ `PaymentStatusConsumer.java` - New Kafka listener service
- ✅ `KafkaTestController.java` - New test controller
- ✅ `application.yaml` - Updated with Kafka and logging configuration
- ✅ `docker-compose.yml` - New Docker Compose setup
- ✅ `build.gradle` - Updated with spring-kafka dependency

## Summary

The implementation provides:
- ✅ Complete Kafka producer (existing PaymentNotificationProducer)
- ✅ Kafka consumer with database persistence
- ✅ Comprehensive logging at both producer and consumer levels
- ✅ Database storage of all published messages
- ✅ Test endpoint for manual testing
- ✅ Docker environment for local development
- ✅ Error handling and retry mechanisms

All published results are logged with details about:
- Topic name
- Partition number
- Message offset
- Timestamp of receipt
- Database save confirmation or errors

