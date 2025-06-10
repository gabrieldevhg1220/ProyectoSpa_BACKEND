package com.backendspa.controller;

import com.backendspa.entity.Cliente;
import com.backendspa.entity.Empleado;
import com.backendspa.entity.Reserva;
import com.backendspa.service.ClienteService;
import com.backendspa.service.EmpleadoService;
import com.backendspa.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private EmpleadoService empleadoService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> createReserva(@RequestBody ReservaRequest reservaRequest) {
        try {
            // Obtener cliente y empleado
            Cliente cliente = clienteService.getClienteById(reservaRequest.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            Empleado empleado = empleadoService.getEmpleadoById(reservaRequest.getEmpleadoId())
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

            // Crear la reserva
            Reserva reserva = new Reserva();
            reserva.setCliente(cliente);
            reserva.setEmpleado(empleado);
            reserva.setFechaReserva(reservaRequest.getFechaReserva());
            reserva.setServicio(Reserva.Servicio.valueOf(reservaRequest.getServicio()));
            reserva.setStatus(Reserva.Status.PENDIENTE);

            reservaService.createReserva(reserva);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Reserva creada exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error al crear la reserva: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'RECEPCIONISTA', 'GERENTE_GENERAL')")
    public List<Reserva> getAllReservas() {
        return reservaService.getAllReservas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reserva> getReservaById(@PathVariable Long id) {
        return reservaService.getReservaById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'GERENTE_GENERAL')")
    public ResponseEntity<?> updateReserva(@PathVariable Long id, @RequestBody ReservaRequest reservaRequest) {
        try {
            Cliente cliente = clienteService.getClienteById(reservaRequest.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            Empleado empleado = empleadoService.getEmpleadoById(reservaRequest.getEmpleadoId())
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

            Reserva reserva = new Reserva();
            reserva.setCliente(cliente);
            reserva.setEmpleado(empleado);
            reserva.setFechaReserva(reservaRequest.getFechaReserva());
            reserva.setServicio(Reserva.Servicio.valueOf(reservaRequest.getServicio()));
            reserva.setStatus(Reserva.Status.valueOf(reservaRequest.getStatus()));

            Reserva updatedReserva = reservaService.updateReserva(id, reserva);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Reserva actualizada exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error al actualizar la reserva: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'GERENTE_GENERAL')")
    public ResponseEntity<?> deleteReserva(@PathVariable Long id) {
        try {
            reservaService.deleteReserva(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Reserva eliminada exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error al eliminar la reserva: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/servicios")
    public List<ServicioDTO> getAllServicios() {
        return Arrays.stream(Reserva.Servicio.values())
                .map(servicio -> new ServicioDTO(servicio.name(), servicio.getDescripcion()))
                .collect(Collectors.toList());
    }

    static class ServicioDTO {
        private final String nombre;
        private final String descripcion;

        public ServicioDTO(String nombre, String descripcion) {
            this.nombre = nombre;
            this.descripcion = descripcion;
        }

        public String getNombre() {
            return nombre;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    static class ReservaRequest {
        private Long clienteId;
        private Long empleadoId;
        private LocalDateTime fechaReserva;
        private String servicio;
        private String status;

        public Long getClienteId() { return clienteId; }
        public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
        public Long getEmpleadoId() { return empleadoId; }
        public void setEmpleadoId(Long empleadoId) { this.empleadoId = empleadoId; }
        public LocalDateTime getFechaReserva() { return fechaReserva; }
        public void setFechaReserva(LocalDateTime fechaReserva) { this.fechaReserva = fechaReserva; }
        public String getServicio() { return servicio; }
        public void setServicio(String servicio) { this.servicio = servicio; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}