package com.backendspa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "pagos")
@Data
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "reserva_id")
    @JsonBackReference // Rompe el ciclo desde la perspectiva de Pago
    private Reserva reserva;

    @Column(nullable = false)
    private Double montoTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Reserva.MedioPago medioPago;

    @Column(nullable = false)
    private LocalDate fechaPago;

    @Column
    private Integer descuentoAplicado;
}