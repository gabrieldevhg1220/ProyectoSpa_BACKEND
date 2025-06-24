package com.backendspa.service;

import com.backendspa.entity.*;
import com.backendspa.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ServicioRepository servicioRepository;
    private final ReservaServicioRepository reservaServicioRepository;
    private final PagoRepository pagoRepository;


    public ReservaService(
            ReservaRepository reservaRepository,
            ClienteRepository clienteRepository,
            EmpleadoRepository empleadoRepository,
            ServicioRepository servicioRepository,
            ReservaServicioRepository reservaServicioRepository,
            PagoRepository pagoRepository
    ) {
        this.reservaRepository = reservaRepository;
        this.clienteRepository = clienteRepository;
        this.empleadoRepository = empleadoRepository;
        this.servicioRepository = servicioRepository;
        this.reservaServicioRepository = reservaServicioRepository;
        this.pagoRepository = pagoRepository;
    }

    public Reserva createReserva(Reserva reserva, List<ReservaServicioDTO> serviciosDTO) {
        // Validar cliente
        Cliente cliente = clienteRepository.findById(reserva.getCliente().getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        // Validar empleado
        Empleado empleado = empleadoRepository.findById(reserva.getEmpleado().getId())
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        if (empleado.getRol() == null) {
            throw new IllegalArgumentException("El empleado debe tener un rol asignado");
        }

        // Validar medio de pago
        if (reserva.getMedioPago() == null) {
            throw new IllegalArgumentException("El medio de pago es obligatorio");
        }

        // Validar fechas (mínimo 48 horas)
        LocalDateTime now = LocalDateTime.now();
        for (ReservaServicioDTO dto : serviciosDTO) {
            long diferenciaHora = ChronoUnit.HOURS.between(now, dto.getFechaServicio());
            if (diferenciaHora < 48) {
                throw new IllegalArgumentException("Las reservas deben realizarse con al menos 48 horas de antelación.");
            }
        }

        // Crear la reserva
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setCliente(cliente);
        nuevaReserva.setEmpleado(empleado);
        nuevaReserva.setFechaReserva(reserva.getFechaReserva());
        nuevaReserva.setStatus(Reserva.Status.PENDIENTE);
        nuevaReserva.setMedioPago(reserva.getMedioPago());
        nuevaReserva.setDescuentoAplicado(reserva.getDescuentoAplicado());
        nuevaReserva.setHistorial(reserva.getHistorial());
        nuevaReserva.setServicios(new ArrayList<>());
        nuevaReserva.setPagos(new ArrayList<>());

        // Agrupar servicios por fecha
        Map<LocalDate, List<ReservaServicioDTO>> serviciosPorDia = serviciosDTO.stream()
                .collect(Collectors.groupingBy(dto -> dto.getFechaServicio().toLocalDate()));

        // Crear pagos por día
        for (Map.Entry<LocalDate, List<ReservaServicioDTO>> entry : serviciosPorDia.entrySet()) {
            LocalDate fechaPago = entry.getKey();
            List<ReservaServicioDTO> serviciosDelDia = entry.getValue();

            // Calcular monto total
            double montoTotal = 0;
            for (ReservaServicioDTO dto : serviciosDelDia) {
                Servicio servicio = servicioRepository.findByNombre(dto.getServicioNombre())
                        .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado: " + dto.getServicioNombre()));
                montoTotal += servicio.getPrecio();
            }

            // Aplicar descuento si corresponde
            if (reserva.getMedioPago() == Reserva.MedioPago.TARJETA_DEBITO && reserva.getDescuentoAplicado() != null) {
                montoTotal *= (1 - reserva.getDescuentoAplicado() / 100.0);
            }

            // Crear pago
            Pago pago = new Pago();
            pago.setCliente(cliente);
            pago.setReserva(nuevaReserva);
            pago.setMontoTotal(montoTotal);
            pago.setMedioPago(reserva.getMedioPago());
            pago.setFechaPago(fechaPago);
            pago.setDescuentoAplicado(reserva.getDescuentoAplicado());
            pago = pagoRepository.save(pago);

            // Añadir pago a la reserva
            nuevaReserva.getPagos().add(pago);

            // Asociar servicios a la reserva
            for (ReservaServicioDTO dto : serviciosDelDia) {
                Servicio servicio = servicioRepository.findByNombre(dto.getServicioNombre())
                        .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado: " + dto.getServicioNombre()));
                ReservaServicio reservaServicio = new ReservaServicio();
                reservaServicio.setReserva(nuevaReserva);
                reservaServicio.setServicio(servicio);
                reservaServicio.setFechaServicio(dto.getFechaServicio());
                nuevaReserva.getServicios().add(reservaServicio);
            }
        }

        // Guardar reserva
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

    public Reserva updateReserva(Long id, Reserva reservaDetails, List<ReservaServicioDTO> serviciosDTO) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Validar medio de pago
        if (reservaDetails.getMedioPago() == null) {
            throw new IllegalArgumentException("El medio de pago es obligatorio");
        }

        // Actualizar campos
        reserva.setCliente(reservaDetails.getCliente());
        reserva.setEmpleado(reservaDetails.getEmpleado());
        reserva.setFechaReserva(reservaDetails.getFechaReserva());
        reserva.setStatus(reservaDetails.getStatus());
        reserva.setMedioPago(reservaDetails.getMedioPago());
        reserva.setDescuentoAplicado(reservaDetails.getDescuentoAplicado());
        reserva.setHistorial(reservaDetails.getHistorial());

        // Limpiar servicios y pagos existentes
        reserva.getServicios().clear();
        reserva.getPagos().clear();

        // Agrupar servicios por fecha para pagos
        Map<LocalDate, List<ReservaServicioDTO>> serviciosPorDia = serviciosDTO.stream()
                .collect(Collectors.groupingBy(dto -> dto.getFechaServicio().toLocalDate()));

        // Crear nuevos pagos
        for (Map.Entry<LocalDate, List<ReservaServicioDTO>> entry : serviciosPorDia.entrySet()) {
            LocalDate fechaPago = entry.getKey();
            List<ReservaServicioDTO> serviciosDelDia = entry.getValue();

            double montoTotal = 0;
            for (ReservaServicioDTO dto : serviciosDelDia) {
                Servicio servicio = servicioRepository.findByNombre(dto.getServicioNombre())
                        .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado: " + dto.getServicioNombre()));
                montoTotal += servicio.getPrecio();
            }

            if (reserva.getMedioPago() == Reserva.MedioPago.TARJETA_DEBITO && reserva.getDescuentoAplicado() != null) {
                montoTotal *= (1 - reserva.getDescuentoAplicado() / 100.0);
            }

            Pago pago = new Pago();
            pago.setCliente(reserva.getCliente());
            pago.setReserva(reserva);
            pago.setMontoTotal(montoTotal);
            pago.setMedioPago(reserva.getMedioPago());
            pago.setFechaPago(fechaPago);
            pago.setDescuentoAplicado(reserva.getDescuentoAplicado());
            pago = pagoRepository.save(pago);

            reserva.getPagos().add(pago);

            for (ReservaServicioDTO dto : serviciosDelDia) {
                Servicio servicio = servicioRepository.findByNombre(dto.getServicioNombre())
                        .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado: " + dto.getServicioNombre()));
                ReservaServicio reservaServicio = new ReservaServicio();
                reservaServicio.setReserva(reserva);
                reservaServicio.setServicio(servicio);
                reservaServicio.setFechaServicio(dto.getFechaServicio());
                reserva.getServicios().add(reservaServicio);
            }
        }

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

    public static class ReservaServicioDTO {
        private String servicioNombre;
        private LocalDateTime fechaServicio;

        public String getServicioNombre() { return servicioNombre; }
        public void setServicioNombre(String servicioNombre) { this.servicioNombre = servicioNombre; }
        public LocalDateTime getFechaServicio() { return fechaServicio; }
        public void setFechaServicio(LocalDateTime fechaServicio) { this.fechaServicio = fechaServicio; }
    }
}