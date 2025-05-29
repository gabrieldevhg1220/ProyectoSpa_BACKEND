package com.backendspa.security;

import com.backendspa.entity.Cliente;
import com.backendspa.entity.Empleado;
import com.backendspa.service.ClienteService;
import com.backendspa.service.EmpleadoService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SpaUserDetailsService implements UserDetailsService {

    private final ClienteService clienteService;
    private final EmpleadoService empleadoService;

    public SpaUserDetailsService(ClienteService clienteService, EmpleadoService empleadoService) {
        this.clienteService = clienteService;
        this.empleadoService = empleadoService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Buscar en clientes
        Optional<Cliente> cliente = clienteService.getClienteByEmail(email);
        if (cliente.isPresent()) {
            return new SpaUserDetails(cliente.get(), "CLIENTE");
        }

        // Buscar en empleados
        Optional<Empleado> empleado = empleadoService.getEmpleadoByEmail(email);
        if (empleado.isPresent()) {
            return new SpaUserDetails(empleado.get(), "EMPLEADO");
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + email);
    }
}