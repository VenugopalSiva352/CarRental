package com.rental.services.CarRental.product.repositories;


import com.rental.services.CarRental.product.entity.VehicleBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Repository
public interface VehicleBookingRepository extends JpaRepository<VehicleBooking, Integer> {
    @Query(value = "SELECT * FROM vehicle_bookings WHERE vehicle_id = :vehicleId",nativeQuery = true)
    List<VehicleBooking> findByVehicleId(int vehicleId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM vehicle_bookings WHERE vehicle_id = :vehicleId AND reservation_id = :reservationId",nativeQuery = true)
    void deleteByVehicleIdAndReservationId(int vehicleId, int reservationId);
}

