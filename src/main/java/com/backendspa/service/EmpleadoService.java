package com.backendspa.service;

import com.backendspa.entity.Empleado;
import com.backendspa.repository.EmpleadoRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Mapa de servicios a roles permitidos
    private static final Map<String, List<Empleado.Rol>> SERVICIO_ROLES = new HashMap<>();

    static {
        SERVICIO_ROLES.put("ANTI_STRESS", Arrays.asList(Empleado.Rol.MASAJISTA_TERAPEUTICO, Empleado.Rol.TERAPEUTA_SPA));
        SERVICIO_ROLES.put("DESCONTRACTURANTE", Arrays.asList(Empleado.Rol.MASAJISTA_TERAPEUTICO));
        SERVICIO_ROLES.put("PIEDRAS_CALIENTES", Arrays.asList(Empleado.Rol.MASAJISTA_TERAPEUTICO, Empleado.Rol.TERAPEUTA_SPA));
        SERVICIO_ROLES.put("CIRCULATORIO", Arrays.asList(Empleado.Rol.MASAJISTA_TERAPEUTICO));
        SERVICIO_ROLES.put("LIFTING_PESTANAS", Arrays.asList(Empleado.Rol.ESTETICISTA));
        SERVICIO_ROLES.put("DEPILACION_FACIAL", Arrays.asList(Empleado.Rol.ESTETICISTA));
        SERVICIO_ROLES.put("BELLEZA_MANOS_PIES", Arrays.asList(Empleado.Rol.ESPECIALISTA_CUIDADO_UNAS));
        SERVICIO_ROLES.put("PUNTA_DIAMANTE", Arrays.asList(Empleado.Rol.TECNICO_ESTETICA_AVANZADA, Empleado.Rol.ESTETICISTA));
        SERVICIO_ROLES.put("LIMPIEZA_PROFUNDA", Arrays.asList(Empleado.Rol.ESTETICISTA));
        SERVICIO_ROLES.put("CRIO_FRECUENCIA_FACIAL", Arrays.asList(Empleado.Rol.TECNICO_ESTETICA_AVANZADA));
        SERVICIO_ROLES.put("VELASLIM", Arrays.asList(Empleado.Rol.TECNICO_ESTETICA_AVANZADA));
        SERVICIO_ROLES.put("DERMOHEALTH", Arrays.asList(Empleado.Rol.TECNICO_ESTETICA_AVANZADA));
        SERVICIO_ROLES.put("CRIOFRECUENCIA", Arrays.asList(Empleado.Rol.TECNICO_ESTETICA_AVANZADA));
        SERVICIO_ROLES.put("ULTRACAVITACION", Arrays.asList(Empleado.Rol.TECNICO_ESTETICA_AVANZADA));
        SERVICIO_ROLES.put("HIDROMASAJES", Arrays.asList(Empleado.Rol.TERAPEUTA_SPA));
        SERVICIO_ROLES.put("YOGA", Arrays.asList(Empleado.Rol.INSTRUCTOR_YOGA));
    }

    // CONSTRUCTOR
    public EmpleadoService(EmpleadoRepository empleadoRepository, @Lazy BCryptPasswordEncoder passwordEncoder) {
        this.empleadoRepository = empleadoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Empleado> getAllEmpleados() {
        return empleadoRepository.findAll();
    }

    public List<Empleado> getEmpleadosForReservas() {
        List<Empleado.Rol> rolesPermitidos = Arrays.asList(
                Empleado.Rol.ESTETICISTA,
                Empleado.Rol.TECNICO_ESTETICA_AVANZADA,
                Empleado.Rol.ESPECIALISTA_CUIDADO_UNAS,
                Empleado.Rol.MASAJISTA_TERAPEUTICO,
                Empleado.Rol.TERAPEUTA_SPA,
                Empleado.Rol.COORDINADOR_AREA,
                Empleado.Rol.RECEPCIONISTA,
                Empleado.Rol.INSTRUCTOR_YOGA,
                Empleado.Rol.NUTRICIONISTA,
                Empleado.Rol.GERENTE_GENERAL
        );
        List<Empleado> empleadosFiltrados = empleadoRepository.findAll().stream()
                .filter(empleado -> rolesPermitidos.contains(empleado.getRol()))
                .collect(Collectors.toList());
        // Log para depuración
        System.out.println("Empleados devueltos por getEmpleadosForReservas():");
        empleadosFiltrados.forEach(empleado ->
                System.out.println(" - " + empleado.getNombre() + " " + empleado.getApellido() + ", Rol: " + empleado.getRol())
        );
        return empleadosFiltrados;
    }

    // Método para obtener empleados según el servicio.
    public List<Empleado> getEmpleadosForServicio(String servicio) {
        List<Empleado.Rol> rolesPermitidos = SERVICIO_ROLES.getOrDefault(servicio, Arrays.asList());
        List<Empleado> empleadosFiltrados = empleadoRepository.findAll().stream()
                .filter(empleado -> rolesPermitidos.contains(empleado.getRol()) || empleado.getRol() == Empleado.Rol.GERENTE_GENERAL)
                .collect(Collectors.toList());

        // Log para depuración
        System.out.println("Empleados devueltos por getEmpleadosForServicio('" + servicio + "'):");
        empleadosFiltrados.forEach(empleado ->
                System.out.println(" - " + empleado.getNombre() + " " + empleado.getApellido() + ", Rol: " + empleado.getRol())
        );
        return empleadosFiltrados;
    }

    public Optional<Empleado> getEmpleadoById(Long id) {
        return empleadoRepository.findById(id);
    }

    public Optional<Empleado> getEmpleadoByEmail(String email) {
        return empleadoRepository.findByEmail(email);
    }

    public Empleado createEmpleado(Empleado empleado) {
        empleado.setPassword(passwordEncoder.encode(empleado.getPassword()));
        return empleadoRepository.save(empleado);
    }

    public Empleado updateEmpleado(Long id, Empleado empleadoDetails) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        empleado.setDni(empleadoDetails.getDni());
        empleado.setNombre(empleadoDetails.getNombre());
        empleado.setApellido(empleadoDetails.getApellido());
        empleado.setEmail(empleadoDetails.getEmail());
        empleado.setPassword(empleadoDetails.getPassword());
        empleado.setTelefono(empleadoDetails.getTelefono());
        empleado.setRol(empleadoDetails.getRol());
        return empleadoRepository.save(empleado);
    }

    public void deleteEmpleado(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        empleadoRepository.delete(empleado);
    }
}