package com.backendspa.service;

import com.backendspa.entity.Empleado;
import com.backendspa.repository.EmpleadoRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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
