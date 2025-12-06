package com.rental.services.CarRental.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VehicleDetails {

    private  int vehicleID;
    private  String vehicleNumber;
    private  String vehicleType;
    private double dailyRentalCost;
    private  String vehicleStatus;
    private int reservationId;
    private LocalDate bookedFrom;
    private LocalDate bookedTo;
}
