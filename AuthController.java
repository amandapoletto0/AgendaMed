package com.amanda.agendamento.controller;

import com.amanda.agendamento.dto.*;
import com.amanda.agendamento.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registro(@Valid @RequestBody RegistroPacienteRequest request) {
        return ResponseEntity.status(201).body(service.registrarPaciente(request));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        service.redefinirSenha(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse> me(@RequestHeader("Authorization") String authorization) {
        var u = service.autenticar(authorization);
        return ResponseEntity.ok(new LoginResponse(u.getTokenSessao(), u.getNome(), u.getEmail(), u.getTipo().name(), u.getPacienteId()));
    }
}
