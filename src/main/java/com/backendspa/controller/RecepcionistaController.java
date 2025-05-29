package com.backendspa.controller;

import com.backendspa.entity.Cliente;
import com.backendspa.entity.Reserva;
import com.backendspa.service.ClienteService;
import com.backendspa.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recepcionista")
public class RecepcionistaController {

    private final ReservaService reservaService;
    private final ClienteService clienteService;

    public RecepcionistaController(ReservaService reservaService, ClienteService clienteService) {
        this.reservaService = reservaService;
        this.clienteService = clienteService;
    }

    // Lista las reservas.
    @GetMapping("/reservas")
    @PreAuthorize("hasRole('ROLE_RECEPCIONISTA')")
    public ResponseEntity<List<Reserva>> getReservasForRecepcionista() {
        return ResponseEntity.ok(reservaService.getAllReservas());
    }

    // Crear una reserva.
    @PostMapping("/reservas")
    @PreAuthorize("hasRole('ROLE_RECEPCIONISTA')")
    public ResponseEntity<Reserva> createReserva(@RequestBody Reserva reserva) {
        try {
            Reserva nuevaReserva = reservaService.createReserva(reserva);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaReserva);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // Actualizar una reserva.
    @PutMapping("/reservas/{id}")
    @PreAuthorize("hasRole('ROLE_RECEPCIONISTA')")
    public ResponseEntity<Reserva> updateReserva(@PathVariable Long id, @RequestBody Reserva reserva) {
        try {
            Reserva updatedReserva = reservaService.updateReserva(id, reserva);
            return ResponseEntity.ok(updatedReserva);
        }catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    
    // Eliminar una reserva
    @DeleteMapping("/reservas/{id}")
    @PreAuthorize("hasRole('ROLE_RECEPCIONISTA')")
    public ResponseEntity<Void> deleteReserva(@PathVariable Long id) {
        try {
            reservaService.deleteReserva(id);
            return ResponseEntity.ok().build();
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Obtiene los clientes.
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
}
