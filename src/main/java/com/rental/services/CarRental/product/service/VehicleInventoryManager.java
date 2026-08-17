package com.rental.services.CarRental.product.service;

import com.rental.services.CarRental.product.dto.ReservationDTO;
import com.rental.services.CarRental.product.dto.VehicleDTO;
import com.rental.services.CarRental.product.entity.VehicleBooking;
import com.rental.services.CarRental.product.entity.VehicleEntity;
import com.rental.services.CarRental.product.enums.VehicleStatus;
import com.rental.services.CarRental.product.enums.VehicleType;
import com.rental.services.CarRental.product.repositories.VehicleBookingRepository;
import com.rental.services.CarRental.product.repositories.VehicleRepository;
import com.rental.services.CarRental.product.utility.DateInterval;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class VehicleInventoryManager {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleBookingRepository vehicleBookingRepository;

    public VehicleEntity addVehicle(VehicleDTO vehicle) {
        VehicleEntity vehicleEntity = VehicleEntity.builder().vehicleNumber(vehicle.getVehicleNumber())
                .vehicleType(vehicle.getVehicleType())
                .vehicleStatus(VehicleStatus.AVAILABLE.toString())
                .dailyRentalCost(vehicle.getDailyRentalCost())
                .build();
        return vehicleRepository.save(vehicleEntity);
    }

    public Optional<VehicleEntity> getVehicle(int vehicleId) {
        return vehicleRepository.findById(vehicleId);
    }

    // Pessimistic locking - locks the row in DB
    @Transactional
    public boolean isAvailable(int vehicleId, LocalDate from, LocalDate to) {
        log.info("Checking availability for vehicleId: {}, from: {}, to: {}", vehicleId, from, to);
        log.info("Acquiring lock for vehicleId: {}", vehicleId);
        VehicleEntity vehicle = vehicleRepository.findByIdWithLock(vehicleId);

        if (vehicle == null) return false;
        if (VehicleStatus.MAINTENANCE.toString().equals(vehicle.getVehicleStatus())) return false;

        DateInterval requested = new DateInterval(from, to);
        log.info("Requested interval: {} to {}", from, to);
        log.info("Fetching bookings for vehicleId: {}", vehicleId);
        List<VehicleBooking> bookings = vehicleBookingRepository.findByVehicleId(vehicleId);

        for (VehicleBooking booking : bookings) {
            DateInterval bookedInterval = new DateInterval(booking.getBookedFrom(), booking.getBookedTo());
            if (bookedInterval.overlaps(requested)) {
                return false;
            }
        }

        return true;
    }

    @Transactional
    public boolean reserve(int vehicleId, LocalDate from, LocalDate to) {

        // Locks the vehicle row for update
        log.info("Reserving vehicleId: {} ", vehicleId);
        log.info("Acquiring ReentrantLock for vehicleId: {}", vehicleId);
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        try{
            Optional<VehicleEntity> vehicleOpt = vehicleRepository.findByIdForUpdate(vehicleId);
            if (vehicleOpt.isEmpty()) {
                log.info("Vehicle not found for vehicleId: {}", vehicleId);
                return false;
            }
            VehicleEntity vehicle = vehicleOpt.get();
            log.info("Fetched vehicle for vehicleId: {} with status: {}", vehicleId, vehicle.getVehicleStatus());

            if (VehicleStatus.MAINTENANCE.toString().equals(vehicle.getVehicleStatus()) || VehicleStatus.BOOKED.toString().equals(vehicle.getVehicleStatus())) {
                log.info("Vehicle is not available for booking.");
                return false;
            }

            if (!isAvailable(vehicleId, from, to)) {
                return false;
            }
            log.info("Vehicle is available, proceeding with booking.");
            VehicleBooking vehicleBooking = new VehicleBooking();
            vehicleBooking.setBookedFrom(from);
            vehicleBooking.setBookedTo(to);
            vehicleBooking.setVehicle(vehicle);
            vehicleBookingRepository.save(vehicleBooking);

            vehicle.setVehicleStatus(VehicleStatus.BOOKED.toString());
            log.info("Updating vehicle status to BOOKED for vehicleId: {}", vehicle.getVehicleID());
            vehicleRepository.save(vehicle);
            return true;}
        finally {
            lock.unlock();
        }
    }

    @Transactional
    public void release(int vehicleId, int reservationId) {
        log.info("Releasing vehicleId: {} for reservationId: {}", vehicleId, reservationId);
        log.info("Acquiring ReentrantLock for vehicleId: {}", vehicleId);
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        try {
            Optional<VehicleEntity> vehicleOpt = vehicleRepository.findByIdForUpdate(vehicleId);
            if (vehicleOpt.isEmpty()) {
                log.warn("Vehicle not found for vehicleId: {}", vehicleId);
                return;
            }
            VehicleEntity vehicle = vehicleOpt.get();
            log.info("Deleting Bills associated with vehicle reservation Id:{}",reservationId);
            vehicleBookingRepository.deleteAssociatedBills(reservationId);
            log.info("Deleting Vehicle Reservation for vehicleId:{} and reservationId:{}",vehicleId,reservationId);
            vehicleBookingRepository.deleteByVehicleIdAndReservationId(vehicleId, reservationId);

            List<VehicleBooking> remainingBookings = vehicleBookingRepository.findByVehicleId(vehicleId);

            if (remainingBookings.isEmpty()) {
                vehicle.setVehicleStatus(VehicleStatus.AVAILABLE.toString());
                vehicleRepository.save(vehicle);
            }
        }finally {
            lock.unlock();
        }
    }

    @Transactional(readOnly = true)
    public List<VehicleEntity> getAvailableVehicles(VehicleType type, LocalDate from, LocalDate to) {
        return vehicleRepository.findAvailableVehicles(type, from, to);
    }

    public List<VehicleEntity> getAllAvailableVehicles(LocalDate from, LocalDate to) {
        return vehicleRepository.findAllAvailableVehicles(from, to);
    }
    public List<VehicleEntity> getAllVehicles() {
        return vehicleRepository.findAll();
    }
    public ResponseEntity<List<VehicleBooking>> getAllBookingsForVehicle(int vehicleId) {
    List<VehicleBooking> bookings = vehicleBookingRepository.findByVehicleId(vehicleId);
    if (bookings == null || bookings.isEmpty()) {
        throw new IllegalArgumentException("No bookings found for vehicleId: " + vehicleId);
    }
    return ResponseEntity.ok(bookings);
}

    public boolean cancelBooking(int vehicleId, int reservationId) {
        release(vehicleId, reservationId);
        return true;
    }
    @Transactional
    public VehicleBooking modifyBooking(int vehicleId, int reservationId, LocalDate newFrom, LocalDate newTo) {
        log.info("Modifying booking for vehicleId: {}, reservationId: {}", vehicleId, reservationId);
        log.info("Acquiring ReentrantLock for vehicleId: {}", vehicleId);
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        try {
            Optional<VehicleEntity> vehicleEntity = vehicleRepository.findByIdForUpdate(vehicleId);

            if (vehicleEntity.isEmpty()) {
                log.warn("Vehicle not found for vehicleId: {}", vehicleId);
                return null;
            }
            List<VehicleBooking> bookings = vehicleBookingRepository.findByVehicleId(vehicleId);
            VehicleBooking bookingToModify = null;
            for (VehicleBooking booking : bookings) {
                if (booking.getReservationId() == reservationId) {
                    bookingToModify = booking;
                    break;
                }
            }
            if (bookingToModify == null) {
                log.warn("Booking not found for reservationId: {}", reservationId);
                return null;
            }
                bookingToModify.setBookedFrom(newFrom);
                bookingToModify.setBookedTo(newTo);
                vehicleBookingRepository.save(bookingToModify);
                log.info("Booking modified successfully for reservationId: {}", reservationId);

            return bookingToModify;
        } finally {
            lock.unlock();
        }
    }

    public VehicleBooking getBookingDetails(int reservationId) {
        return vehicleBookingRepository.findById(reservationId).orElse(null);
    }

    @Transactional
    public List<ReservationDTO> getAllReservations() {
        List<Object[]> allBookingsWithVehicleInfo = vehicleBookingRepository.findAllBookingsWithVehicleInfo();
        // Convert the JPQL results into ReservationDTOs
        List<ReservationDTO> reservationDTOs = allBookingsWithVehicleInfo.stream().map(row -> {
            VehicleBooking booking = (VehicleBooking) row[0];
            VehicleEntity vehicle = (VehicleEntity) row[1];
            return ReservationDTO.builder()
                    .reservationId(booking.getReservationId())
                    .vehicleNumber(vehicle.getVehicleNumber())
                    .vehicleType(vehicle.getVehicleType())
                    .dailyRentalCost(vehicle.getDailyRentalCost())
                    .vehicleStatus(vehicle.getVehicleStatus())
                    .bookedFrom(booking.getBookedFrom())
                    .bookedTo(booking.getBookedTo())
                    .build();
        }).toList();
        return reservationDTOs;
    }

}
