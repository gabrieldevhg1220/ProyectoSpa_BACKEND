package com.backendspa.repository;

import com.backendspa.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByClienteId(Long clienteId);
    List<Reserva> findByEmpleadoId(Long empleadoId);

    // Método para buscar reservas por empleado y rango de fechas
    List<Reserva> findByEmpleadoIdAndFechaReservaBetween(Long empleadoId, LocalDateTime startDate, LocalDateTime endDate);
}
