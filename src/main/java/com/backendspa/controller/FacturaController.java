package com.backendspa.controller;

import com.backendspa.entity.Cliente;
import com.backendspa.entity.Pago;
import com.backendspa.entity.Reserva;
import com.backendspa.entity.ReservaServicio;
import com.backendspa.service.ClienteService;
import com.backendspa.service.EmailService;
import com.backendspa.service.ReservaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/factura")
public class FacturaController {

    private static final Logger logger = LoggerFactory.getLogger(FacturaController.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ReservaService reservaService;

    @PostMapping("/send-invoice")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_RECEPCIONISTA', 'ROLE_GERENTE_GENERAL')")
    public ResponseEntity<Map<String, Object>> sendInvoice(@RequestBody InvoiceRequest request) {
        try {
            logger.info("Recibida solicitud para enviar factura con invoiceNumber: {}", request.getInvoiceNumber());

            // Validar que los campos requeridos no sean nulos
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "El email es requerido."));
            }
            if (request.getInvoiceNumber() == null || request.getInvoiceNumber().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "El número de factura es requerido."));
            }
            if (request.getAttachmentBase64() == null || request.getAttachmentBase64().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "El adjunto es requerido."));
            }

            // Obtener el email del usuario autenticado desde el token JWT
            String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!request.getEmail().equals(authenticatedEmail)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "El email proporcionado no coincide con el usuario autenticado: " + authenticatedEmail));
            }

            // Obtener el cliente por email
            Cliente cliente = clienteService.getClienteByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado con email: " + request.getEmail()));

            // Obtener reservas asociadas al cliente
            List<Reserva> reservas = reservaService.getReservasByClienteId(cliente.getId());

            // Extraer la fecha de invoiceNumber (formato INV-YYYYMMDD)
            LocalDate fechaFactura;
            try {
                String datePart = request.getInvoiceNumber().substring(4); // Quitar "INV-"
                fechaFactura = LocalDate.parse(datePart, DateTimeFormatter.BASIC_ISO_DATE);
            } catch (DateTimeParseException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Formato de número de factura inválido. Se espera INV-YYYYMMDD."));
            }

            // Agrupar servicios por fecha
            Map<LocalDate, List<ReservaServicio>> serviciosPorDia = reservas.stream()
                    .flatMap(reserva -> reserva.getServicios().stream())
                    .collect(Collectors.groupingBy(rs -> rs.getFechaServicio().toLocalDate()));

            // Obtener servicios para la fecha solicitada
            List<ReservaServicio> serviciosDelDia = serviciosPorDia.getOrDefault(fechaFactura, List.of());

            if (serviciosDelDia.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "No hay servicios para la fecha especificada en la factura."));
            }

            // Obtener la reserva relevante (la más reciente para la fecha)
            Reserva reserva = reservas.stream()
                    .filter(r -> r.getServicios().stream().anyMatch(rs -> rs.getFechaServicio().toLocalDate().equals(fechaFactura)))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No se encontró reserva para la fecha especificada"));

            // Calcular monto total original
            double valorOriginal = serviciosDelDia.stream()
                    .mapToDouble(rs -> rs.getServicio().getPrecio())
                    .sum();
            double descuento = reserva.getPagos().stream()
                    .mapToInt(Pago::getDescuentoAplicado)
                    .filter(d -> d > 0)
                    .findFirst()
                    .orElse(0);
            double valorConDescuento = reserva.getPagos().stream()
                    .filter(p -> p.getFechaPago().equals(fechaFactura))
                    .mapToDouble(Pago::getMontoTotal)
                    .findFirst()
                    .orElse(valorOriginal * (1 - descuento / 100.0));

            // Preparar detalles de la factura para el frontend
            Map<String, Object> facturaDetalles = new HashMap<>();
            facturaDetalles.put("clienteNombre", cliente.getNombre() + " " + (cliente.getApellido() != null ? cliente.getApellido() : ""));
            facturaDetalles.put("dni", cliente.getDni() != null ? cliente.getDni() : "N/A");
            facturaDetalles.put("email", cliente.getEmail() != null ? cliente.getEmail() : "N/A");
            facturaDetalles.put("fechaReserva", reserva.getFechaReserva().toString());
            facturaDetalles.put("servicios", serviciosDelDia.stream()
                    .map(rs -> Map.of(
                            "nombre", rs.getServicio().getNombre(),
                            "precio", rs.getServicio().getPrecio(),
                            "fecha", rs.getFechaServicio().toString()
                    )).collect(Collectors.toList()));
            facturaDetalles.put("medioPago", reserva.getMedioPago().getDescripcion());
            facturaDetalles.put("valorOriginal", valorOriginal);
            facturaDetalles.put("descuento", descuento > 0 ? descuento : null);
            facturaDetalles.put("valorConDescuento", valorConDescuento);
            facturaDetalles.put("invoiceNumber", request.getInvoiceNumber());

            // Generar contenido del email para confirmación
            StringBuilder body = new StringBuilder();
            body.append("Estimado/a ").append(cliente.getNombre()).append(",\n");
            body.append("Gracias por elegir Sentirse Bien. Adjuntamos su factura.\n");
            body.append("Detalles de los servicios:\n");
            for (ReservaServicio rs : serviciosDelDia) {
                body.append("- ").append(rs.getServicio().getNombre())
                        .append(" (Precio: $").append(String.format("%.2f", rs.getServicio().getPrecio()))
                        .append(", Fecha: ").append(rs.getFechaServicio()).append(")\n");
            }
            body.append("Medio de pago: ").append(reserva.getMedioPago().getDescripcion()).append("\n");
            body.append("Valor original: $").append(String.format("%.2f", valorOriginal)).append("\n");
            if (reserva.getMedioPago() == Reserva.MedioPago.TARJETA_DEBITO && descuento > 0) {
                body.append("Descuento (15%): $").append(String.format("%.2f", valorOriginal * (descuento / 100.0))).append("\n");
            }
            body.append("Total con descuento: $").append(String.format("%.2f", valorConDescuento)).append("\n");

            // Enviar correo con el PDF adjunto
            emailService.sendEmailWithAttachment(
                    request.getEmail(),
                    "Factura " + request.getInvoiceNumber(),
                    body.toString(),
                    request.getAttachmentBase64(),
                    "Factura_" + request.getInvoiceNumber() + ".pdf"
            );

            return ResponseEntity.ok(facturaDetalles);
        } catch (IOException e) {
            logger.error("Error al enviar el correo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al enviar el correo: " + e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error al procesar la factura: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Error al procesar la factura: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado al procesar la factura: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error inesperado al procesar la factura: " + e.getMessage()));
        }
    }

    public static class InvoiceRequest {
        private String email;
        private String invoiceNumber;
        private String attachmentBase64;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getInvoiceNumber() { return invoiceNumber; }
        public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
        public String getAttachmentBase64() { return attachmentBase64; }
        public void setAttachmentBase64(String attachmentBase64) { this.attachmentBase64 = attachmentBase64; }
    }
}