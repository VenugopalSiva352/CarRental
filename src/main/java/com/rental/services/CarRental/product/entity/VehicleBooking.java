package com.rental.services.CarRental.product.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "vehicle_bookings")
@AllArgsConstructor
@Data
public class VehicleBooking {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int reservationId;
    private LocalDate bookedFrom;
    private LocalDate bookedTo;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "vehicle_id")
    private VehicleEntity vehicle;

    public VehicleBooking() {
    }

}
