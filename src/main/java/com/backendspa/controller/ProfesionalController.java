package com.backendspa.controller;

import com.backendspa.entity.Empleado;
import com.backendspa.entity.Reserva;
import com.backendspa.security.SpaUserDetails;
import com.backendspa.service.EmpleadoService;
import com.backendspa.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/profesional")
public class ProfesionalController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private EmpleadoService empleadoService;

    private static final List<String> ROLES_PERMITIDOS = Arrays.asList(
            "ROLE_ESTETICISTA", "ROLE_TECNICO_ESTETICA_AVANZADA", "ROLE_ESPECIALISTA_CUIDADO_UNAS", "ROLE_MASAJISTA_TERAPEUTICO",
            "ROLE_TERAPEUTA_SPA", "ROLE_INSTRUCTOR_YOGA", "ROLE_NUTRICIONISTA"
    );

    @GetMapping("/reservas/hoy")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Reserva>> getReservaHoy() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Obtener el usuario autenticado como SpaUserDetails
        SpaUserDetails userDetails = (SpaUserDetails) authentication.getPrincipal();
        Long empleadoId = userDetails.getId();

        // Buscar el Empleado en la base de datos
        Empleado empleado = empleadoService.getEmpleadoById(empleadoId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + empleadoId));

        // Verifica que el empleado tenga un rol que esté permitido
        String rol = "ROLE_" + empleado.getRol().name();
        if (!ROLES_PERMITIDOS.contains(rol)) {
            return ResponseEntity.status(403).body(null); // Forbidden
        }

        LocalDate hoy = LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires"));
        LocalDateTime startOfDay = hoy.atStartOfDay(); // Convertir LocalDate a LocalDateTime (00:00:00)
        List<Reserva> reservas = reservaService.getReservasByEmpleadoAndDate(empleado.getId(), startOfDay);
        return ResponseEntity.ok(reservas);
    }
}