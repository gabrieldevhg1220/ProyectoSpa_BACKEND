package com.backendspa;

import com.backendspa.entity.*;
import com.backendspa.repository.*;
import com.backendspa.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class BackendSpaApplicationTests {

	@Autowired
	private ReservaService reservaService;

	@Autowired
	private ReservaRepository reservaRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private EmpleadoRepository empleadoRepository;

	@Autowired
	private ServicioRepository servicioRepository;

	@Autowired
	private ReservaServicioRepository reservaServicioRepository;

	@Autowired
	private PagoRepository pagoRepository;

	private Cliente cliente;
	private Empleado empleado;
	private Servicio servicio1;
	private Servicio servicio2;

	@BeforeEach
	@Transactional
	void setUp() {
		// Limpiar la base de datos
		reservaServicioRepository.deleteAll();
		pagoRepository.deleteAll();
		reservaRepository.deleteAll();
		clienteRepository.deleteAll();
		empleadoRepository.deleteAll();
		servicioRepository.deleteAll();

		// Crear un cliente
		cliente = new Cliente();
		cliente.setDni("12345678A"); // Valor único y no nulo
		cliente.setNombre("Test Cliente");
		cliente.setApellido("Apellido");
		cliente.setEmail("test@cliente.com");
		cliente.setPassword("encoded_password");
		cliente.setTelefono("1234567890");
		cliente = clienteRepository.save(cliente);

		// Crear un empleado
		empleado = new Empleado();
		empleado.setDni("87654321B"); // Valor único y no nulo
		empleado.setNombre("Test Empleado");
		empleado.setApellido("Apellido");
		empleado.setEmail("test@empleado.com");
		empleado.setRol(Empleado.Rol.MASAJISTA_TERAPEUTICO);
		empleado.setPassword("encoded_password");
		empleado.setTelefono("0987654321");
		empleado = empleadoRepository.save(empleado);

		// Crear servicios
		servicio1 = new Servicio();
		servicio1.setNombre("ANTI_STRESS");
		servicio1.setPrecio(100.0);
		servicio1 = servicioRepository.save(servicio1);

		servicio2 = new Servicio();
		servicio2.setNombre("DESCONTRACTURANTE");
		servicio2.setPrecio(120.0);
		servicio2 = servicioRepository.save(servicio2);
	}

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void testMultipleServicesSameDay() {
		Reserva reserva = new Reserva();
		reserva.setCliente(cliente);
		reserva.setEmpleado(empleado);
		reserva.setFechaReserva(LocalDateTime.now().plusDays(3));
		reserva.setMedioPago(Reserva.MedioPago.TARJETA_DEBITO);
		reserva.setDescuentoAplicado(15);
		reserva.setStatus(Reserva.Status.PENDIENTE);

		List<ReservaService.ReservaServicioDTO> serviciosDTO = new ArrayList<>();
		ReservaService.ReservaServicioDTO dto1 = new ReservaService.ReservaServicioDTO();
		dto1.setServicioNombre("ANTI_STRESS");
		dto1.setFechaServicio(LocalDateTime.now().plusDays(3));
		serviciosDTO.add(dto1);

		ReservaService.ReservaServicioDTO dto2 = new ReservaService.ReservaServicioDTO();
		dto2.setServicioNombre("DESCONTRACTURANTE");
		dto2.setFechaServicio(LocalDateTime.now().plusDays(3));
		serviciosDTO.add(dto2);

		Reserva savedReserva = reservaService.createReserva(reserva, serviciosDTO);

		assertEquals(1, savedReserva.getPagos().size());
		assertEquals(2, savedReserva.getServicios().size());
		assertEquals(15, savedReserva.getDescuentoAplicado());
		assertEquals(187.0, savedReserva.getPagos().get(0).getMontoTotal(), 0.01);
	}

	@Test
	@Transactional
	void testMultipleServicesDifferentDays() {
		Reserva reserva = new Reserva();
		reserva.setCliente(cliente);
		reserva.setEmpleado(empleado);
		reserva.setFechaReserva(LocalDateTime.now().plusDays(3));
		reserva.setMedioPago(Reserva.MedioPago.TARJETA_DEBITO);
		reserva.setDescuentoAplicado(15);
		reserva.setStatus(Reserva.Status.PENDIENTE);

		List<ReservaService.ReservaServicioDTO> serviciosDTO = new ArrayList<>();
		ReservaService.ReservaServicioDTO dto1 = new ReservaService.ReservaServicioDTO();
		dto1.setServicioNombre("ANTI_STRESS");
		dto1.setFechaServicio(LocalDateTime.now().plusDays(3));
		serviciosDTO.add(dto1);

		ReservaService.ReservaServicioDTO dto2 = new ReservaService.ReservaServicioDTO();
		dto2.setServicioNombre("DESCONTRACTURANTE");
		dto2.setFechaServicio(LocalDateTime.now().plusDays(4));
		serviciosDTO.add(dto2);

		Reserva savedReserva = reservaService.createReserva(reserva, serviciosDTO);

		assertEquals(2, savedReserva.getPagos().size());
		assertEquals(2, savedReserva.getServicios().size());
		assertEquals(15, savedReserva.getDescuentoAplicado());
		assertEquals(100.0 * 0.85, savedReserva.getPagos().stream()
				.filter(p -> p.getFechaPago().equals(LocalDateTime.now().plusDays(3).toLocalDate()))
				.findFirst().get().getMontoTotal(), 0.01);
		assertEquals(120.0 * 0.85, savedReserva.getPagos().stream()
				.filter(p -> p.getFechaPago().equals(LocalDateTime.now().plusDays(4).toLocalDate()))
				.findFirst().get().getMontoTotal(), 0.01);
	}

	@Test
	@Transactional
	void testReservaLessThan48Hours() {
		Reserva reserva = new Reserva();
		reserva.setCliente(cliente);
		reserva.setEmpleado(empleado);
		reserva.setFechaReserva(LocalDateTime.now().plusHours(24));
		reserva.setMedioPago(Reserva.MedioPago.TARJETA_DEBITO);
		reserva.setDescuentoAplicado(15);
		reserva.setStatus(Reserva.Status.PENDIENTE);

		List<ReservaService.ReservaServicioDTO> serviciosDTO = new ArrayList<>();
		ReservaService.ReservaServicioDTO dto = new ReservaService.ReservaServicioDTO();
		dto.setServicioNombre("ANTI_STRESS");
		dto.setFechaServicio(LocalDateTime.now().plusHours(24));
		serviciosDTO.add(dto);

		assertThrows(IllegalArgumentException.class, () -> {
			reservaService.createReserva(reserva, serviciosDTO);
		}, "Las reservas deben realizarse con al menos 48 horas de antelación.");
	}
}