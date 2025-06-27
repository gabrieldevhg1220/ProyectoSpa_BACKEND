package com.backendspa.controller;

import com.backendspa.entity.*;
import com.backendspa.repository.ServicioRepository;
import com.backendspa.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
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

    @Autowired
    private ServicioRepository servicioRepository;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> createReserva(@RequestBody ReservaRequest reservaRequest) {
        try {
            if (reservaRequest.getMedioPago() == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "El medio de pago es obligatorio");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (reservaRequest.getServicios() == null || reservaRequest.getServicios().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Debe seleccionar al menos un servicio");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Cliente cliente = clienteService.getClienteById(reservaRequest.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            Empleado empleado = empleadoService.getEmpleadoById(reservaRequest.getEmpleadoId())
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

            Reserva reserva = new Reserva();
            reserva.setCliente(cliente);
            reserva.setEmpleado(empleado);
            reserva.setFechaReserva(reservaRequest.getFechaReserva());
            reserva.setStatus(Reserva.Status.PENDIENTE);
            reserva.setMedioPago(Reserva.MedioPago.valueOf(reservaRequest.getMedioPago()));
            reserva.setDescuentoAplicado(reservaRequest.getDescuentoAplicado());

            List<ReservaService.ReservaServicioDTO> serviciosDTO = reservaRequest.getServicios().stream()
                    .map(s -> {
                        ReservaService.ReservaServicioDTO dto = new ReservaService.ReservaServicioDTO();
                        dto.setServicioNombre(s.getServicio());
                        dto.setFechaServicio(s.getFechaServicio());
                        return dto;
                    }).collect(Collectors.toList());

            Reserva savedReserva = reservaService.createReserva(reserva, serviciosDTO);

            // Obtener detalles de la factura desde el último pago
            Pago ultimoPago = savedReserva.getPagos().get(savedReserva.getPagos().size() - 1);
            double valorOriginal = savedReserva.getServicios().stream()
                    .mapToDouble(rs -> rs.getServicio().getPrecio())
                    .sum();
            double descuento = ultimoPago.getDescuentoAplicado() != null ? ultimoPago.getDescuentoAplicado() : 0;
            double valorConDescuento = ultimoPago.getMontoTotal();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reserva creada exitosamente");
            response.put("reservaId", savedReserva.getId());
            response.put("factura", new ReservaService.FacturaDetalles(
                    savedReserva.getMedioPago(),
                    valorOriginal,
                    descuento,
                    valorConDescuento
            ));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
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
            if (reservaRequest.getMedioPago() == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "El medio de pago es obligatorio");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (reservaRequest.getServicios() == null || reservaRequest.getServicios().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Debe seleccionar al menos un servicio");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Cliente cliente = clienteService.getClienteById(reservaRequest.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            Empleado empleado = empleadoService.getEmpleadoById(reservaRequest.getEmpleadoId())
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

            Reserva reserva = new Reserva();
            reserva.setCliente(cliente);
            reserva.setEmpleado(empleado);
            reserva.setFechaReserva(reservaRequest.getFechaReserva());
            reserva.setStatus(Reserva.Status.valueOf(reservaRequest.getStatus()));
            reserva.setMedioPago(Reserva.MedioPago.valueOf(reservaRequest.getMedioPago()));
            reserva.setDescuentoAplicado(reservaRequest.getDescuentoAplicado());

            List<ReservaService.ReservaServicioDTO> serviciosDTO = reservaRequest.getServicios().stream()
                    .map(s -> {
                        ReservaService.ReservaServicioDTO dto = new ReservaService.ReservaServicioDTO();
                        dto.setServicioNombre(s.getServicio());
                        dto.setFechaServicio(s.getFechaServicio());
                        return dto;
                    }).collect(Collectors.toList());

            Reserva updatedReserva = reservaService.updateReserva(id, reserva, serviciosDTO);

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
        return servicioRepository.findAll().stream()
                .map(servicio -> new ServicioDTO(servicio.getNombre(), servicio.getDescripcion()))
                .collect(Collectors.toList());
    }

    static class ServicioDTO {
        private final String nombre;
        private final String descripcion;

        public ServicioDTO(String nombre, String descripcion) {
            this.nombre = nombre;
            this.descripcion = descripcion;
        }

        public String getNombre() { return nombre; }
        public String getDescripcion() { return descripcion; }
    }

    static class ReservaRequest {
        private Long clienteId;
        private Long empleadoId;
        private LocalDateTime fechaReserva;
        private List<ServicioReservaDTO> servicios;
        private String status;
        private String medioPago;
        private Integer descuentoAplicado;

        public Long getClienteId() { return clienteId; }
        public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
        public Long getEmpleadoId() { return empleadoId; }
        public void setEmpleadoId(Long empleadoId) { this.empleadoId = empleadoId; }
        public LocalDateTime getFechaReserva() { return fechaReserva; }
        public void setFechaReserva(LocalDateTime fechaReserva) { this.fechaReserva = fechaReserva; }
        public List<ServicioReservaDTO> getServicios() { return servicios; }
        public void setServicios(List<ServicioReservaDTO> servicios) { this.servicios = servicios; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMedioPago() { return medioPago; }
        public void setMedioPago(String medioPago) { this.medioPago = medioPago; }
        public Integer getDescuentoAplicado() { return descuentoAplicado; }
        public void setDescuentoAplicado(Integer descuentoAplicado) { this.descuentoAplicado = descuentoAplicado; }
    }

    static class ServicioReservaDTO {
        private String servicio;
        private LocalDateTime fechaServicio;

        public String getServicio() { return servicio; }
        public void setServicio(String servicio) { this.servicio = servicio; }
        public LocalDateTime getFechaServicio() { return fechaServicio; }
        public void setFechaServicio(LocalDateTime fechaServicio) { this.fechaServicio = fechaServicio; }
    }
}