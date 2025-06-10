package com.backendspa.controller;

import com.backendspa.entity.Empleado;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RolController {
    @GetMapping
    @PreAuthorize("hasRole('ROLE_GERENTE_GENERAL')")
    public ResponseEntity<List<String>> getAllRoles() {
        List<String> roles = Arrays.stream(Empleado.Rol.values())
                .map(rol -> "ROLE_" + rol.name())
                .collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }
}
