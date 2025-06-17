package com.backendspa.service;

import com.backendspa.entity.Cliente;
import com.backendspa.entity.Empleado;
import com.backendspa.entity.Reserva;
import com.backendspa.repository.ClienteRepository;
import com.backendspa.repository.EmpleadoRepository;
import com.backendspa.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;

    public ReservaService(ReservaRepository reservaRepository, ClienteRepository clienteRepository, EmpleadoRepository empleadoRepository) {
        this.reservaRepository = reservaRepository;
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
    }

    public Reserva createReserva(Reserva reserva) {
        // Log para depuración
        System.out.println("Fecha recibida en createReserva: " + reserva.getFechaReserva());

        // Validar que el cliente exista
        if (reserva.getCliente() == null || reserva.getCliente().getId() == null) {
            throw new IllegalArgumentException("El ID del cliente no puede ser nulo");
        }
        Cliente cliente = clienteRepository.findById(reserva.getCliente().getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente con ID " + reserva.getCliente().getId() + " no encontrado"));

        // Validar que el empleado exista
        if (reserva.getEmpleado() == null || reserva.getEmpleado().getId() == null) {
            throw new IllegalArgumentException("El ID del empleado no puede ser nulo");
        }
        Empleado empleado = empleadoRepository.findById(reserva.getEmpleado().getId())
                .orElseThrow(() -> new IllegalArgumentException("Empleado con ID " + reserva.getEmpleado().getId() + " no encontrado"));

        // Validar que el empleado tenga un rol asignado
        if (empleado.getRol() == null) {
            throw new IllegalArgumentException("El empleado debe tener un rol asignado");
        }

        // Validar que la reserva sea al menos 48hs antes
        LocalDateTime now = LocalDateTime.now();
        long diferenciaHora = ChronoUnit.HOURS.between(now, reserva.getFechaReserva());
        if (diferenciaHora < 48) {
            throw new IllegalArgumentException("Las reservas solo pueden realizarse con al menos 48 horas de antelación.");
        }

        // Validar que el medio de pago no sea nulo
        if (reserva.getMedioPago() == null) {
            throw new IllegalArgumentException("El medio de pago es obligatorio");
        }

        // Crear una nueva instancia de Reserva para evitar problemas con la deserialización
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setCliente(cliente);
        nuevaReserva.setEmpleado(empleado);
        nuevaReserva.setFechaReserva(reserva.getFechaReserva());
        nuevaReserva.setServicio(reserva.getServicio());
        nuevaReserva.setStatus(reserva.getStatus());
        nuevaReserva.setMedioPago(reserva.getMedioPago());
        nuevaReserva.setDescuentoAplicado(reserva.getDescuentoAplicado()); // Persistir el descuento
        nuevaReserva.setHistorial(reserva.getHistorial());

        // Log para depuración después de guardar
        Reserva savedReserva = reservaRepository.save(nuevaReserva);
        System.out.println("Fecha guardada en la base de datos: " + savedReserva.getFechaReserva());
        return savedReserva;
    }

    public List<Reserva> getReservasByClienteId(Long clienteId) {
        return reservaRepository.findByClienteId(clienteId);
    }

    public List<Reserva> getAllReservas() {
        return reservaRepository.findAll();
    }

    public Optional<Reserva> getReservaById(Long id) {
        return reservaRepository.findById(id);
    }

    public Reserva updateReserva(Long id, Reserva reservaDetails) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        // Validar que el medio de pago no sea nulo
        if (reservaDetails.getMedioPago() == null) {
            throw new IllegalArgumentException("El medio de pago es obligatorio");
        }
        reserva.setCliente(reservaDetails.getCliente());
        reserva.setEmpleado(reservaDetails.getEmpleado());
        reserva.setFechaReserva(reservaDetails.getFechaReserva());
        reserva.setServicio(reservaDetails.getServicio());
        reserva.setStatus(reservaDetails.getStatus());
        reserva.setMedioPago(reservaDetails.getMedioPago());
        reserva.setDescuentoAplicado(reservaDetails.getDescuentoAplicado()); // Persistir el descuento
        reserva.setHistorial(reservaDetails.getHistorial());
        return reservaRepository.save(reserva);
    }

    public void deleteReserva(Long id) {
        reservaRepository.deleteById(id);
    }

    public List<Reserva> getReservasByEmpleadoAndDate(Long empleadoId, LocalDateTime date) {
        LocalDateTime startOfDay = date.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = date.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return reservaRepository.findByEmpleadoIdAndFechaReservaBetween(empleadoId, startOfDay, endOfDay);
    }
}