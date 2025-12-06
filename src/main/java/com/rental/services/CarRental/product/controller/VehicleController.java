package com.rental.services.CarRental.product.controller;

import com.rental.services.CarRental.product.entity.VehicleBooking;
import com.rental.services.CarRental.product.entity.VehicleEntity;
import com.rental.services.CarRental.product.service.VehicleInventoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class VehicleController {
    @Autowired
    private VehicleInventoryManager vehicleInventoryManager;

    @PostMapping("/vehicles/save")
    public VehicleEntity saveVehicle(@RequestBody VehicleEntity vehicle){
        return vehicleInventoryManager.addVehicle(vehicle);
    }

    @PostMapping("/vehicles/reserve")
    public Boolean reserveVehicle(@RequestParam("vehicleId") int vehicleId,
                                  @RequestParam("from") LocalDate from,
                                  @RequestParam("to") LocalDate to){
        return vehicleInventoryManager.reserve(vehicleId, from, to);
    }
    @GetMapping("/vehicles/available")
    public List<VehicleEntity> getAllAvailableVehicles(@RequestParam("from") LocalDate from,
                                                     @RequestParam("to") LocalDate to){
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
                                                    @RequestParam("newFrom") LocalDate newFrom,
                                                    @RequestParam("newTo") LocalDate newTo){
        VehicleBooking updatedBooking = vehicleInventoryManager.modifyBooking(vehicleId, reservationId, newFrom, newTo);
        if (updatedBooking != null) {
            return ResponseEntity.ok(updatedBooking);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
