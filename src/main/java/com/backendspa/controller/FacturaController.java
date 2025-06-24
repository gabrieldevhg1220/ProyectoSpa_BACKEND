package com.backendspa.controller;

import com.backendspa.entity.Cliente;
import com.backendspa.entity.Reserva;
import com.backendspa.entity.ReservaServicio;
import com.backendspa.service.ClienteService;
import com.backendspa.service.EmailService;
import com.backendspa.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/factura")
public class FacturaController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ReservaService reservaService;

    @PostMapping("/send-invoice")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_RECEPCIONISTA', 'ROLE_GERENTE_GENERAL')")
    public ResponseEntity<String> sendInvoice(@RequestBody InvoiceRequest request) {
        try {
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
                        .body("Formato de número de factura inválido. Se espera INV-YYYYMMDD.");
            }

            // Agrupar servicios por fecha
            Map<LocalDate, List<ReservaServicio>> serviciosPorDia = reservas.stream()
                    .flatMap(reserva -> reserva.getServicios().stream())
                    .collect(Collectors.groupingBy(rs -> rs.getFechaServicio().toLocalDate()));

            // Obtener servicios para la fecha solicitada
            List<ReservaServicio> serviciosDelDia = serviciosPorDia.getOrDefault(fechaFactura, List.of());

            if (serviciosDelDia.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No hay servicios para la fecha especificada en la factura.");
            }

            // Calcular monto total
            double montoTotal = serviciosDelDia.stream()
                    .mapToDouble(rs -> rs.getServicio().getPrecio())
                    .sum();

            // Aplicar descuento si aplica
            Integer descuento = reservas.stream()
                    .filter(r -> r.getDescuentoAplicado() != null)
                    .map(Reserva::getDescuentoAplicado)
                    .findFirst()
                    .orElse(0);
            if (descuento > 0) {
                montoTotal *= (1 - descuento / 100.0);
            }

            // Generar contenido del email
            StringBuilder body = new StringBuilder();
            body.append("Estimado/a ").append(cliente.getNombre()).append(",\n");
            body.append("Gracias por elegir Sentirse Bien. Adjuntamos su factura.\n");
            body.append("Detalles de los servicios:\n");
            for (ReservaServicio rs : serviciosDelDia) {
                body.append("- ").append(rs.getServicio().getNombre())
                        .append(" (Fecha: ").append(rs.getFechaServicio()).append(")\n");
            }
            body.append("Total: $").append(String.format("%.2f", montoTotal)).append("\n");

            // Enviar correo con el PDF adjunto
            emailService.sendEmailWithAttachment(
                    request.getEmail(),
                    "Factura " + request.getInvoiceNumber(),
                    body.toString(),
                    request.getAttachmentBase64(),
                    "Factura_" + request.getInvoiceNumber() + ".pdf"
            );

            return ResponseEntity.ok("Comprobante enviado al correo: " + request.getEmail());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar el correo: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al procesar la factura: " + e.getMessage());
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