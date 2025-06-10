package com.backendspa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    @GetMapping
    public ResponseEntity<List<String>> getServicios() {
        // Lista estática de servicios (puedes obtenerla de una base de datos si lo prefieres)
        List<String> servicios = Arrays.asList(
                "ANTI_STRESS",
                "MASAJE_DESCONTRACTURANTE",
                "PIEDRAS_CALIENTES",
                "CIRCULATORIO",
                "LIFTING_PESTANAS",
                "DEPILACION_FACIAL",
                "BELLEZA_MANOS_PIES",
                "PUNTA_DIAMANTE",
                "LIMPIEZA_PROFUNDA",
                "CRIO_FRECUENCIA_FACIAL",
                "VELASLIM",
                "DERMOHEALTH",
                "CRIOFRECUENCIA",
                "ULTRACAVITACION",
                "HIDROMASAJES",
                "YOGA"
        );
        return ResponseEntity.ok(servicios);
    }
}