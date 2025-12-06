package com.rental.services.CarRental.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
@Entity
@Table(name = "bills")
@AllArgsConstructor
public class BillEntity {
    @Id
    @Column(name = "bill_id")
    private int billId;
    @OneToOne
    @JoinColumn(name = "reservation_id")
    private VehicleBooking reservationId;
    private double totalBillAmount;
    private boolean billPaid;

    public boolean getBillPaid() {
        return billPaid;
    }
}
