package com.backendspa.controller;

import com.backendspa.entity.Cliente;
import com.backendspa.entity.Empleado;
import com.backendspa.entity.Reserva;
import com.backendspa.service.ClienteService;
import com.backendspa.service.EmpleadoService;
import com.backendspa.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final EmpleadoService empleadoService;
    private final ClienteService clienteService;
    private final ReservaService reservaService;

    public AdminController(EmpleadoService empleadoService, ClienteService clienteService, ReservaService reservaService) {
        this.empleadoService = empleadoService;
        this.clienteService = clienteService;
        this.reservaService = reservaService;
    }

    // CRUD para Empleados
    @GetMapping("/empleados")
    public ResponseEntity<List<Empleado>> getAllEmpleados() {
        return ResponseEntity.ok(empleadoService.getAllEmpleados());
    }

    @GetMapping("/empleados/{id}")
    public ResponseEntity<Empleado> getEmpleadoById(@PathVariable Long id) {
        return empleadoService.getEmpleadoById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/empleados")
    public ResponseEntity<Empleado> createEmpleado(@RequestBody Empleado empleado) {
        try {
            Empleado nuevoEmpleado = empleadoService.createEmpleado(empleado);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEmpleado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/empleados/{id}")
    public ResponseEntity<Empleado> updateEmpleado(@PathVariable Long id, @RequestBody Empleado empleado) {
        try {
            Empleado updatedEmpleado = empleadoService.updateEmpleado(id, empleado);
            return ResponseEntity.ok(updatedEmpleado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/empleados/{id}")
    public ResponseEntity<Void> deleteEmpleado(@PathVariable Long id) {
        try {
            empleadoService.deleteEmpleado(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // CRUD para Clientes
    @GetMapping("/clientes")
    public ResponseEntity<List<Cliente>> getAllClientes() {
        return ResponseEntity.ok(clienteService.getAllClientes());
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<Cliente> getClienteById(@PathVariable Long id) {
        return clienteService.getClienteById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/clientes")
    public ResponseEntity<Cliente> createCliente(@RequestBody Cliente cliente) {
        try {
            Cliente nuevoCliente = clienteService.createCliente(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCliente);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/clientes/{id}")
    public ResponseEntity<Cliente> updateCliente(@PathVariable Long id, @RequestBody Cliente cliente) {
        try {
            Cliente updatedCliente = clienteService.updateCliente(id, cliente);
            return ResponseEntity.ok(updatedCliente);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        try {
            clienteService.deleteCliente(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // CRUD para Reservas
    @GetMapping("/reservas")
    public ResponseEntity<List<Reserva>> getAllReservas() {
        return ResponseEntity.ok(reservaService.getAllReservas());
    }

    @GetMapping("/reservas/{id}")
    public ResponseEntity<Reserva> getReservaById(@PathVariable Long id) {
        return reservaService.getReservaById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/reservas")
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
    public ResponseEntity<Void> deleteReserva(@PathVariable Long id) {
        try {
            reservaService.deleteReserva(id);
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