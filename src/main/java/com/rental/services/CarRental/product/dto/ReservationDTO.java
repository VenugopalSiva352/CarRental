package com.rental.services.CarRental.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDTO {
    private int reservationId;
    private LocalDate bookedFrom;
    private LocalDate bookedTo;
    // flattened vehicle fields for convenience
    private String vehicleNumber;
    private String vehicleType;
    private double dailyRentalCost;
    private String vehicleStatus;
}
