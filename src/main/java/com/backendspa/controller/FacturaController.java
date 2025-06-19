package com.backendspa.controller;

import com.backendspa.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/factura")
public class FacturaController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-invoice")
    @PreAuthorize("hasAnyRole('CLIENTE', 'RECEPCIONISTA')")
    public ResponseEntity<String> sendInvoice(
            @RequestParam("email") String email,
            @RequestParam("invoice") MultipartFile invoice,
            @RequestParam("invoiceNumber") String invoiceNumber) {
        try {
            emailService.sendEmailWithAttachment(
                    email,
                    "Comprobante de Pago - Sentirse Bien",
                    "Adjunto encontrarás tu comprobante de pago. Gracias por elegir Sentirse Bien!",
                    invoice.getBytes(),
                    "factura_" + invoiceNumber + ".pdf"
            );
            return ResponseEntity.ok("Comprobante enviado al correo: " + email);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error al enviar el comprobante: " + e.getMessage());
        }
    }
}