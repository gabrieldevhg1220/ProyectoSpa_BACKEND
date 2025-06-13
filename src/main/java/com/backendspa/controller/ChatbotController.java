package com.backendspa.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @PostMapping("/message")
    public ResponseEntity<Map<String, String>> handleMessage(@RequestBody Map<String, String> request){
        String message = request.get("message").toLowerCase();
        Map<String, String> response = new HashMap<>();
        switch (message) {
            case "servicios":
                response.put("reply", "Ofrecemos masajes relajantes, tratamientos faciales y servicios grupales como yoga.");
                break;
            case "horarios":
                response.put("reply", "Estamos abuertos de lunes a sábado de 9:00 a 20:00");
                break;
            case "contacto":
                response.put("reply", "Estamos en French 414, Resistencia, Chaco | Tel: (362) 456-7890");
                break;
            default:
                response.put("reply", "Lo siento no entiendo. Escribe 'ayuda' para mas opciones");
        }
        return ResponseEntity.ok(response);
    }
}
