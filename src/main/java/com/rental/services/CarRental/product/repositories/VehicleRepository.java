package com.rental.services.CarRental.product.repositories;


import com.rental.services.CarRental.product.entity.VehicleEntity;
import com.rental.services.CarRental.product.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, Integer> {

    @Query(value = "SELECT * FROM vehicle WHERE vehicle_id = :vehicleId",nativeQuery = true)
    VehicleEntity findByIdWithLock(@Param("vehicleId") int vehicleId);

    @Query(value = "SELECT * FROM vehicle WHERE vehicle_id = :vehicleId FOR UPDATE", nativeQuery = true)
    Optional<VehicleEntity> findByIdForUpdate(@Param("vehicleId") int vehicleId);

    @Query(value = "SELECT DISTINCT v.* FROM vehicle v " +
            "LEFT JOIN vehicle_bookings vb ON v.vehicle_id = vb.vehicle_id " +
            "WHERE v.vehicle_type = :type AND v.vehicle_status != 'MAINTENANCE' " +
            "AND (vb.vehicle_id IS NULL OR NOT EXISTS (" +
            "  SELECT 1 FROM vehicle_bookings vb2 " +
            "  WHERE vb2.vehicle_id = v.vehicle_id " +
            "  AND vb2.booked_from < :to AND vb2.booked_to > :from" +
            "))", nativeQuery = true)
    List<VehicleEntity> findAvailableVehicles(@Param("type") VehicleType type,
                                        @Param("from") LocalDate from,
                                        @Param("to") LocalDate to);
    @Query(value = "SELECT DISTINCT v.* FROM vehicle v " +
            "LEFT JOIN vehicle_bookings vb ON v.vehicle_id = vb.vehicle_id " +
            "WHERE v.vehicle_status NOT IN ('MAINTENANCE','RENTED')  " +
            "AND (vb.vehicle_id IS NULL OR NOT EXISTS (" +
            "  SELECT 1 FROM vehicle_bookings vb2 " +
            "  WHERE vb2.vehicle_id = v.vehicle_id " +
            "  AND vb2.booked_from < :to AND vb2.booked_to > :from" +
            "))", nativeQuery = true)
    List<VehicleEntity> findAllAvailableVehicles(LocalDate from, LocalDate to);
}

