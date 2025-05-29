package com.backendspa.service;

import com.backendspa.entity.Cliente;
import com.backendspa.entity.Empleado;
import com.backendspa.entity.Reserva;
import com.backendspa.repository.ClienteRepository;
import com.backendspa.repository.EmpleadoRepository;
import com.backendspa.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

        // Crear una nueva instancia de Reserva para evitar problemas con la deserialización
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setCliente(cliente);
        nuevaReserva.setEmpleado(empleado);
        nuevaReserva.setFechaReserva(reserva.getFechaReserva());
        nuevaReserva.setServicio(reserva.getServicio());
        nuevaReserva.setStatus(reserva.getStatus());

        return reservaRepository.save(nuevaReserva);
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
        reserva.setCliente(reservaDetails.getCliente());
        reserva.setEmpleado(reservaDetails.getEmpleado());
        reserva.setFechaReserva(reservaDetails.getFechaReserva());
        reserva.setServicio(reservaDetails.getServicio());
        reserva.setStatus(reservaDetails.getStatus());
        return reservaRepository.save(reserva);
    }

    public void deleteReserva(Long id) {
        reservaRepository.deleteById(id);
    }
}
