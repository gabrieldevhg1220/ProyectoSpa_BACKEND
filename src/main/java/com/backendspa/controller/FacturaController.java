package com.backendspa.controller;

import com.backendspa.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/factura")
public class FacturaController {

    @Autowired
    private EmailService emailService;

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

    @PostMapping("/send-invoice")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_RECEPCIONISTA', 'ROLE_GERENTE_GENERAL')")
    public ResponseEntity<String> sendInvoice(@RequestBody InvoiceRequest request) {
        try {
            emailService.sendEmailWithAttachment(
                    request.getEmail(),
                    "Comprobante de Pago - Sentirse Bien",
                    "Adjunto encontrarás tu comprobante de pago. Gracias por elegir Sentirse Bien!",
                    request.getAttachmentBase64(),
                    "factura_" + request.getInvoiceNumber() + ".pdf"
            );
            return ResponseEntity.ok("Comprobante enviado al correo: " + request.getEmail());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error al enviar el comprobante: " + e.getMessage());
        }
    }
}