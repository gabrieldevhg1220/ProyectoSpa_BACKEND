package com.backendspa.controller;

import com.backendspa.entity.Cliente;
import com.backendspa.security.SpaUserDetails;
import com.backendspa.service.ClienteService;
import com.backendspa.entity.Reserva;
import com.backendspa.service.ReservaService;
import com.backendspa.entity.Empleado;
import com.backendspa.service.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private EmpleadoService empleadoService;

    @PostMapping
    public Cliente createCliente(@RequestBody Cliente cliente) {
        return clienteService.createCliente(cliente);
    }

    @PostMapping("/reservas")
    public ResponseEntity<?> createReserva(@RequestBody ReservaRequest reservaRequest) {
        try {
            // Validar campos requeridos
            if (reservaRequest.clienteId == null || reservaRequest.empleadoId == null ||
                    reservaRequest.fechaReserva == null || reservaRequest.servicios == null ||
                    reservaRequest.servicios.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Faltan campos requeridos");
                errorResponse.put("message", "Los campos cliente, empleado, fechaReserva y servicios son obligatorios");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // Cargar el cliente desde la base de datos
            Cliente cliente = clienteService.getClienteById(reservaRequest.clienteId)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

            // Cargar el empleado desde la base de datos
            Empleado empleado = empleadoService.getEmpleadoById(reservaRequest.empleadoId)
                    .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

            // Crear la reserva
            Reserva reserva = new Reserva();
            reserva.setCliente(cliente);
            reserva.setEmpleado(empleado);
            reserva.setFechaReserva(reservaRequest.fechaReserva);
            reserva.setStatus(Reserva.Status.PENDIENTE);
            reserva.setMedioPago(Reserva.MedioPago.valueOf(reservaRequest.medioPago));
            reserva.setDescuentoAplicado(reservaRequest.descuentoAplicado);

            // Convertir servicios a DTO
            List<ReservaService.ReservaServicioDTO> serviciosDTO = reservaRequest.servicios.stream()
                    .map(s -> {
                        ReservaService.ReservaServicioDTO dto = new ReservaService.ReservaServicioDTO();
                        dto.setServicioNombre(s.servicio);
                        dto.setFechaServicio(s.fechaServicio);
                        return dto;
                    }).collect(Collectors.toList());

            // Guardar la reserva
            Reserva nuevaReserva = reservaService.createReserva(reserva, serviciosDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaReserva);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error en los datos proporcionados");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor");
            errorResponse.put("message", "Error al crear la reserva: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{clienteId}/reservas")
    public ResponseEntity<?> getReservasByClienteId(@PathVariable Long clienteId, Authentication authentication) {
        try {
            // Obtener el usuario autenticado
            SpaUserDetails userDetails = (SpaUserDetails) authentication.getPrincipal();
            Long authenticatedClienteId = userDetails.getId();

            // Verificar que el cliente autenticado solo pueda ver sus propias reservas
            if (!authenticatedClienteId.equals(clienteId)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Acceso denegado");
                errorResponse.put("message", "No tienes permiso para ver las reservas de otro cliente");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            // Obtener las reservas del cliente
            List<Reserva> reservas = reservaService.getReservasByClienteId(clienteId);
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor");
            errorResponse.put("message", "Error al obtener las reservas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    public List<Cliente> getAllClientes() {
        return clienteService.getAllClientes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> getClienteById(@PathVariable Long id) {
        return clienteService.getClienteById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/actual")
    public ResponseEntity<Cliente> getClienteByToken(Authentication authentication) {
        SpaUserDetails userDetails = (SpaUserDetails) authentication.getPrincipal();
        Long clienteId = userDetails.getId();
        return clienteService.getClienteById(clienteId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @PutMapping("/{id}")
    public Cliente updateCliente(@PathVariable Long id, @RequestBody Cliente clienteDetails) {
        return clienteService.updateCliente(id, clienteDetails);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        clienteService.deleteCliente(id);
        return ResponseEntity.ok().build();
    }

    public static class ReservaRequest {
        public Long clienteId;
        public Long empleadoId;
        public LocalDateTime fechaReserva;
        public List<ServicioDTO> servicios;
        public String medioPago;
        public Integer descuentoAplicado;
    }

    public static class ServicioDTO {
        public String servicio;
        public LocalDateTime fechaServicio;
    }
}