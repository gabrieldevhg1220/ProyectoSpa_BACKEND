package com.backendspa.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
@Data
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(nullable = false)
    private LocalDateTime fechaReserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Servicio servicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public enum Status {
        PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA
    }


    public enum Servicio {
        // Masajes
        ANTI_STRESS("Masaje Anti-stress"),
        DESCONTRACTURANTE("Masaje Descontracturante"),
        PIEDRAS_CALIENTES("Masaje con Piedras Calientes"),
        CIRCULATORIO("Masaje Circulatorio"),
        // Belleza
        LIFTING_PESTANAS("Lifting de Pestañas"),
        DEPILACION_FACIAL("Depilación Facial"),
        BELLEZA_MANOS_PIES("Belleza de Manos y Pies"),
        // Tratamientos Faciales
        PUNTA_DIAMANTE("Punta de Diamante: Microexfoliación"),
        LIMPIEZA_PROFUNDA("Limpieza Profunda + Hidratación"),
        CRIO_FRECUENCIA_FACIAL("Crio Frecuencia Facial"),
        // Tratamientos Corporales
        VELASLIM("VelaSlim: Reducción de Celulitis"),
        DERMOHEALTH("DermoHealth: Drenaje Linfático"),
        CRIOFRECUENCIA("Criofrecuencia: Efecto Lifting"),
        ULTRACAVITACION("Ultracavitación: Técnica Reductora"),
        // Servicios Grupales
        HIDROMASAJES("Hidromasajes"),
        YOGA("Yoga");

        private final String descripcion;

        Servicio(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}
