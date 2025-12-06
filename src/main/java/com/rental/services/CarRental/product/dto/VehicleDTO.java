package com.rental.services.CarRental.product.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VehicleDTO {
    @NonNull
    private  String vehicleNumber;
    private  String vehicleType;
    private double dailyRentalCost;

}
