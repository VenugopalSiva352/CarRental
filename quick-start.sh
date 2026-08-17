#!/bin/bash

# Car Rental - Kafka Integration Quick Start Script

set -e

echo "========================================"
echo "Car Rental - Kafka Integration Setup"
echo "========================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PROJECT_PATH="/Users/gopal/Documents/STS3_ARM_ARC/CarRental"

# Function to print colored output
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_warning "Docker not found. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    print_warning "Docker Compose not found. Please install Docker Compose first."
    exit 1
fi

print_status "Docker and Docker Compose found"
echo ""

# Step 1: Start Docker containers
print_info "Step 1: Starting Kafka, Zookeeper, and MySQL containers..."
cd "$PROJECT_PATH"
docker-compose up -d

sleep 5

print_status "Containers started"
docker-compose ps
echo ""

# Step 2: Create Kafka topic
print_info "Step 2: Creating Kafka topic 'rental-payment-topic'..."
docker exec kafka kafka-topics --create \
  --topic rental-payment-topic \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1 \
  --if-not-exists 2>/dev/null || true

print_status "Kafka topic created or already exists"
echo ""

# Step 3: Build the application
print_info "Step 3: Building the application..."
./gradlew clean build -x test > /dev/null 2>&1

print_status "Application built successfully"
echo ""

# Step 4: Display connection information
print_info "Step 4: Connection Information"
echo ""
echo "MySQL:"
echo "  Host: localhost"
echo "  Port: 3306"
echo "  Username: root"
echo "  Password: root@root"
echo "  Database: CarRental"
echo ""

echo "Kafka:"
echo "  Bootstrap Server: localhost:9092"
echo "  Topic: rental-payment-topic"
echo "  Consumer Group: car-rental-group"
echo ""

# Step 5: Instructions
print_info "Step 5: Next Steps"
echo ""
echo "1. Start the application:"
echo "   cd $PROJECT_PATH"
echo "   ./gradlew bootRun"
echo ""
echo "2. Test the Kafka integration (in another terminal):"
echo "   # Basic test:"
echo "   curl -X POST 'http://localhost:8080/api/kafka/test-payment-message'"
echo ""
echo "   # Test with custom values:"
echo "   curl -X POST 'http://localhost:8080/api/kafka/test-payment-message?paymentId=123&billId=456&totalBillAmount=15000&amountPaid=15000'"
echo ""
echo "3. View logs:"
echo "   # Follow application logs"
echo "   tail -f nohup.out"
echo ""
echo "4. Query saved data from database:"
echo "   mysql -h localhost -u root -proot@root CarRental"
echo "   SELECT * FROM payment_status_logs ORDER BY saved_at DESC LIMIT 5;"
echo ""
echo "5. Monitor Kafka messages:"
echo "   docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic rental-payment-topic --from-beginning"
echo ""
echo "6. Check consumer group status:"
echo "   docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group car-rental-group --describe"
echo ""

# Step 6: Cleanup instructions
print_warning "To stop everything:"
echo ""
echo "1. Stop the application (Ctrl+C in the terminal where it's running)"
echo ""
echo "2. Stop containers:"
echo "   cd $PROJECT_PATH"
echo "   docker-compose down"
echo ""
echo "3. Remove volumes (optional):"
echo "   docker-compose down -v"
echo ""

print_status "Setup complete! Ready to run the application."
echo ""
echo "========================================"
echo "For detailed documentation, see:"
echo "  $PROJECT_PATH/KAFKA_INTEGRATION.md"
echo "========================================"

