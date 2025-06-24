package com.backendspa.controller;

import com.backendspa.entity.Cliente;
import com.backendspa.entity.Reserva;
import com.backendspa.entity.Empleado;
import com.backendspa.service.ClienteService;
import com.backendspa.service.EmpleadoService;
import com.backendspa.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recepcionista")
public class RecepcionistaController {

    private final ReservaService reservaService;
    private final ClienteService clienteService;

    @Autowired
    private EmpleadoService empleadoService;

    public RecepcionistaController(ReservaService reservaService, ClienteService clienteService) {
        this.reservaService = reservaService;
        this.clienteService = clienteService;
    }

    @GetMapping("/reservas")
    @PreAuthorize("hasRole('ROLE_RECEPCIONISTA')")
    public ResponseEntity<List<Reserva>> getReservasForRecepcionista() {
        return ResponseEntity.ok(reservaService.getAllReservas());
    }

    @PostMapping("/reservas")
    @PreAuthorize("hasRole('ROLE_RECEPCIONISTA')")
    public ResponseEntity<Reserva> createReserva(@RequestBody ReservaRequest reservaRequest) {
        try {
            Cliente cliente = clienteService.getClienteById(reservaRequest.clienteId)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
            Empleado empleado = empleadoService.getEmpleadoById(reservaRequest.empleadoId)
                    .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

            Reserva reserva = new Reserva();
            reserva.setCliente(cliente);
            reserva.setEmpleado(empleado);
            reserva.setFechaReserva(reservaRequest.fechaReserva);
            reserva.setStatus(Reserva.Status.PENDIENTE);
            reserva.setMedioPago(Reserva.MedioPago.valueOf(reservaRequest.medioPago));
            reserva.setDescuentoAplicado(reservaRequest.descuentoAplicado);

            List<ReservaService.ReservaServicioDTO> serviciosDTO = reservaRequest.servicios.stream()
                    .map(s -> {
                        ReservaService.ReservaServicioDTO dto = new ReservaService.ReservaServicioDTO();
                        dto.setServicioNombre(s.servicio);
                        dto.setFechaServicio(s.fechaServicio);
                        return dto;
                    }).collect(Collectors.toList());

            Reserva nuevaReserva = reservaService.createReserva(reserva, serviciosDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaReserva);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/reservas/{id}")
    @PreAuthorize("hasRole('ROLE_RECEPCIONISTA')")
    public ResponseEntity<Reserva> updateReserva(@PathVariable Long id, @RequestBody ReservaRequest reservaRequest) {
        try {
            Cliente cliente = clienteService.getClienteById(reservaRequest.clienteId)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
            Empleado empleado = empleadoService.getEmpleadoById(reservaRequest.empleadoId)
                    .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

            Reserva reserva = new Reserva();
            reserva.setCliente(cliente);
            reserva.setEmpleado(empleado);
            reserva.setFechaReserva(reservaRequest.fechaReserva);
            reserva.setStatus(Reserva.Status.valueOf(reservaRequest.status));
            reserva.setMedioPago(Reserva.MedioPago.valueOf(reservaRequest.medioPago));
            reserva.setDescuentoAplicado(reservaRequest.descuentoAplicado);

            List<ReservaService.ReservaServicioDTO> serviciosDTO = reservaRequest.servicios.stream()
                    .map(s -> {
                        ReservaService.ReservaServicioDTO dto = new ReservaService.ReservaServicioDTO();
                        dto.setServicioNombre(s.servicio);
                        dto.setFechaServicio(s.fechaServicio);
                        return dto;
                    }).collect(Collectors.toList());

            Reserva updatedReserva = reservaService.updateReserva(id, reserva, serviciosDTO);
            return ResponseEntity.ok(updatedReserva);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/reservas/{id}")
    @PreAuthorize("hasRole('ROLE_RECEPCIONISTA')")
    public ResponseEntity<Void> deleteReserva(@PathVariable Long id) {
        try {
            reservaService.deleteReserva(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/clientes")
    @PreAuthorize("hasRole('ROLE_RECEPCIONISTA')")
    public ResponseEntity<List<Cliente>> getClientesForRecepcionista() {
        return ResponseEntity.ok(clienteService.getAllClientes());
    }

    @GetMapping("/clientes/{id}")
    @PreAuthorize("hasRole('ROLE_RECEPCIONISTA')")
    public ResponseEntity<Cliente> getClienteById(@PathVariable Long id) {
        return clienteService.getClienteById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/clientes")
    @PreAuthorize("hasRole('ROLE_RECEPCIONISTA')")
    public ResponseEntity<Cliente> createCliente(@RequestBody Cliente cliente) {
        try {
            Cliente nuevoCliente = clienteService.createCliente(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCliente);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/clientes/{id}")
    @PreAuthorize("hasRole('ROLE_RECEPCIONISTA')")
    public ResponseEntity<Cliente> updateCliente(@PathVariable Long id, @RequestBody Cliente cliente) {
        try {
            Cliente updatedCliente = clienteService.updateCliente(id, cliente);
            return ResponseEntity.ok(updatedCliente);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/clientes/{id}")
    @PreAuthorize("hasRole('ROLE_RECEPCIONISTA')")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        try {
            clienteService.deleteCliente(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public static class ReservaRequest {
        public Long clienteId;
        public Long empleadoId;
        public LocalDateTime fechaReserva;
        public List<ServicioDTO> servicios;
        public String status;
        public String medioPago;
        public Integer descuentoAplicado;
    }

    public static class ServicioDTO {
        public String servicio;
        public LocalDateTime fechaServicio;
    }
}