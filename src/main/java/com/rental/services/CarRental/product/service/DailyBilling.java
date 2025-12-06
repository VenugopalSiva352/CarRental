package com.rental.services.CarRental.product.service;

import com.rental.services.CarRental.product.dto.Bill;
import com.rental.services.CarRental.product.entity.BillEntity;
import com.rental.services.CarRental.product.entity.VehicleBooking;
import com.rental.services.CarRental.product.entity.VehicleEntity;
import com.rental.services.CarRental.product.repositories.BillingRepository;
import com.rental.services.CarRental.product.repositories.VehicleBookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
@Service
@Slf4j
public class DailyBilling implements BillingStrategy {
    @Autowired
    private BillingRepository billingRepository;
    @Autowired
    private VehicleBookingRepository vehicleBookingRepository;

    private final AtomicInteger billIdGenerator = new AtomicInteger(5000);
    @Autowired
    private VehicleInventoryManager vehicleInventoryManager;
    public DailyBilling(VehicleInventoryManager vehicleInventoryManager) {
        this.vehicleInventoryManager = vehicleInventoryManager;
    }
    @Override
    public Bill generateBill(int booking) {
        log.info("Generating daily bill for booking id: {}", booking);
        BillEntity billEntity = new BillEntity();
        log.info("Fetching booking details for booking id: {}", booking);
        VehicleBooking bookingDetails = vehicleInventoryManager.getBookingDetails(booking);
        log.info("Calculating total bill amount for booking id: {}", booking);
        Optional<VehicleEntity> vehicle = vehicleInventoryManager.getVehicle(bookingDetails.getVehicle().getVehicleID());
        double dailyRentalCost = vehicle.get().getDailyRentalCost();
        Bill bill = new Bill();
        int billId = billIdGenerator.incrementAndGet();
        bill.setBillId(billId);
        bill.setReservationId(bookingDetails.getReservationId());
        long days = bookingDetails.getBookedFrom().until(bookingDetails.getBookedTo()).getDays() + 1;
        double total = days * dailyRentalCost;
        bill.setTotalBillAmount(total);

        billEntity.setBillId(bill.getBillId());
        billEntity.setReservationId(bookingDetails);
        billEntity.setTotalBillAmount(bill.getTotalBillAmount());
        log.info("Saving bill entity for booking id: {}", booking);
        billingRepository.save(billEntity);
        return bill;
    }

    @Override
    public List<Bill> getAllBills(int vehicleId) {
        log.info("Fetching all bills from repository");
        log.info("Fetching all bookings for vehicle id: {}", vehicleId);
        List<VehicleBooking> vehicleBookings = vehicleBookingRepository.findByVehicleId(vehicleId);
        List<Bill> allBills = new ArrayList<>();
        for(VehicleBooking booking : vehicleBookings){
            log.info("Fetching bill for reservation id: {}", booking.getReservationId());
            Optional<BillEntity> billEntity = billingRepository.findByReservationId(booking.getReservationId());
            if(billEntity.isPresent()){
                Bill bill = new Bill();
                bill.setBillId(billEntity.get().getBillId());
                bill.setReservationId(billEntity.get().getReservationId().getReservationId());
                bill.setTotalBillAmount(billEntity.get().getTotalBillAmount());
                bill.setBillPaid(billEntity.get().getBillPaid());
                allBills.add(bill);
                log.info("Bill found: {}", bill);
            } else {
                log.info("No bill found for reservation id: {}", booking.getReservationId());
            }
        }
        return allBills;
    }

}
