package com.rental.services.CarRental.product.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicle")
@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class VehicleEntity {
    @Id
    @Column(name = "vehicle_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private  int vehicleID;
    private  String vehicleNumber;
    private  String vehicleType;
    private double dailyRentalCost;
    private  String vehicleStatus;

}
