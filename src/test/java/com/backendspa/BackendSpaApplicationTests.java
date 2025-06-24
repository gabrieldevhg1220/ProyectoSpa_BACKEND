package com.backendspa;

import com.backendspa.entity.*;
import com.backendspa.repository.*;
import com.backendspa.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
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

	@Configuration
	static class TestConfig {
		@Bean
		public ReservaService reservaService(
				ReservaRepository reservaRepository,
				ClienteRepository clienteRepository,
				EmpleadoRepository empleadoRepository,
				ServicioRepository servicioRepository,
				ReservaServicioRepository reservaServicioRepository,
				PagoRepository pagoRepository
		) {
			return new ReservaService(
					reservaRepository,
					clienteRepository,
					empleadoRepository,
					servicioRepository,
					reservaServicioRepository,
					pagoRepository
			);
		}
	}

	@BeforeEach
	void setUp() {
		// Limpiar la base de datos
		reservaServicioRepository.deleteAll();
		pagoRepository.deleteAll();
		reservaRepository.deleteAll();
		clienteRepository.deleteAll();
		empleadoRepository.deleteAll();
		servicioRepository.deleteAll();

		// Crear un cliente
		Cliente cliente = new Cliente();
		cliente.setId(1L);
		cliente.setNombre("Test Cliente");
		cliente.setApellido("Apellido");
		cliente.setEmail("test@cliente.com");
		clienteRepository.save(cliente);

		// Crear un empleado
		Empleado empleado = new Empleado();
		empleado.setId(1L);
		empleado.setNombre("Test Empleado");
		empleado.setApellido("Apellido");
		empleado.setEmail("test@empleado.com");
		empleado.setRol(Empleado.Rol.MASAJISTA_TERAPEUTICO);
		empleadoRepository.save(empleado);

		// Crear servicios
		Servicio servicio1 = new Servicio();
		servicio1.setNombre("ANTI_STRESS");
		servicio1.setPrecio(100.0);
		servicioRepository.save(servicio1);

		Servicio servicio2 = new Servicio();
		servicio2.setNombre("DESCONTRACTURANTE");
		servicio2.setPrecio(120.0);
		servicioRepository.save(servicio2);
	}

	@Test
	void contextLoads() {
	}

	@Test
	void testMultipleServicesSameDay() {
		Cliente cliente = new Cliente();
		cliente.setId(1L);
		Empleado empleado = new Empleado();
		empleado.setId(1L);
		empleado.setRol(Empleado.Rol.MASAJISTA_TERAPEUTICO);

		Reserva reserva = new Reserva();
		reserva.setCliente(cliente);
		reserva.setEmpleado(empleado);
		reserva.setFechaReserva(LocalDateTime.now().plusDays(3));
		reserva.setMedioPago(Reserva.MedioPago.TARJETA_DEBITO);
		reserva.setDescuentoAplicado(15);

		List<ReservaService.ReservaServicioDTO> serviciosDTO = new ArrayList<>();
		serviciosDTO.add(new ReservaService.ReservaServicioDTO() {{
			setServicioNombre("ANTI_STRESS");
			setFechaServicio(LocalDateTime.now().plusDays(3));
		}});
		serviciosDTO.add(new ReservaService.ReservaServicioDTO() {{
			setServicioNombre("DESCONTRACTURANTE");
			setFechaServicio(LocalDateTime.now().plusDays(3));
		}});

		Reserva savedReserva = reservaService.createReserva(reserva, serviciosDTO);

		assertEquals(1, savedReserva.getPagos().size());
		assertEquals(2, savedReserva.getServicios().size());
		assertEquals(15, savedReserva.getDescuentoAplicado());
	}

	@Test
	void testMultipleServicesDifferentDays() {
		Cliente cliente = new Cliente();
		cliente.setId(1L);
		Empleado empleado = new Empleado();
		empleado.setId(1L);
		empleado.setRol(Empleado.Rol.MASAJISTA_TERAPEUTICO);

		Reserva reserva = new Reserva();
		reserva.setCliente(cliente);
		reserva.setEmpleado(empleado);
		reserva.setFechaReserva(LocalDateTime.now().plusDays(3));
		reserva.setMedioPago(Reserva.MedioPago.TARJETA_DEBITO);
		reserva.setDescuentoAplicado(15);

		List<ReservaService.ReservaServicioDTO> serviciosDTO = new ArrayList<>();
		serviciosDTO.add(new ReservaService.ReservaServicioDTO() {{
			setServicioNombre("ANTI_STRESS");
			setFechaServicio(LocalDateTime.now().plusDays(3));
		}});
		serviciosDTO.add(new ReservaService.ReservaServicioDTO() {{
			setServicioNombre("DESCONTRACTURANTE");
			setFechaServicio(LocalDateTime.now().plusDays(4));
		}});

		Reserva savedReserva = reservaService.createReserva(reserva, serviciosDTO);

		assertEquals(2, savedReserva.getPagos().size());
		assertEquals(2, savedReserva.getServicios().size());
		assertEquals(15, savedReserva.getDescuentoAplicado());
	}
}