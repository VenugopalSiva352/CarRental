package com.rental.services.CarRental.product.controller;

import com.rental.services.CarRental.product.dto.ReservationDTO;
import com.rental.services.CarRental.product.dto.VehicleDTO;
import com.rental.services.CarRental.product.entity.VehicleBooking;
import com.rental.services.CarRental.product.entity.VehicleEntity;
import com.rental.services.CarRental.product.repositories.VehicleBookingRepository;
import com.rental.services.CarRental.product.service.VehicleInventoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class VehicleController {
    @Autowired
    private VehicleInventoryManager vehicleInventoryManager;
    @Autowired
    private VehicleBookingRepository vehicleBookingRepository;

    @PostMapping("/vehicles/save")
    public VehicleEntity saveVehicle(@Validated @RequestBody VehicleDTO vehicle){
        return vehicleInventoryManager.addVehicle(vehicle);
    }

    @PostMapping("/vehicles/reserve")
    public Boolean reserveVehicle(@RequestParam("vehicleId") int vehicleId,
                                  @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                  @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to){
        return vehicleInventoryManager.reserve(vehicleId, from, to);
    }
    @GetMapping("/vehicles/available")
    public List<VehicleEntity> getAllAvailableVehicles(@RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                     @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to){
        return vehicleInventoryManager.getAllAvailableVehicles(from, to);
    }
    @GetMapping("/vehicles/bookings")
    public List<VehicleBooking> getAllBookingsForVehicle(@RequestParam("vehicleId") int vehicleId){
        return vehicleInventoryManager.getAllBookingsForVehicle(vehicleId);
    }
    @DeleteMapping("/vehicles/cancel")
    public boolean cancelBooking(@RequestParam("vehicleId") int vehicleId,
                                 @RequestParam("reservationId") int reservationId){
        return vehicleInventoryManager.cancelBooking(vehicleId, reservationId);
    }
    @PutMapping("/vehicles/modify")
    public ResponseEntity<VehicleBooking> modifyBooking(@RequestParam("vehicleId") int vehicleId,
                                                    @RequestParam("reservationId") int reservationId,
                                                    @RequestParam("newFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newFrom,
                                                    @RequestParam("newTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newTo){
        VehicleBooking updatedBooking = vehicleInventoryManager.modifyBooking(vehicleId, reservationId, newFrom, newTo);
        if (updatedBooking != null) {
            return ResponseEntity.ok(updatedBooking);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("vehicles/all")
    public ResponseEntity<List<VehicleEntity>> getAllVehicles(){
        List<VehicleEntity> vehicles = vehicleInventoryManager.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("Vehicle/allReservations")
    public ResponseEntity<List<ReservationDTO>> getAllReservations(){
        List<ReservationDTO> bookings = vehicleInventoryManager.getAllReservations();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("Vehicle/bookingDetails")
    public List<Object[]> getBookingDetails() {
        return vehicleBookingRepository.findAllBookingsWithVehicleInfo();

    }
}
