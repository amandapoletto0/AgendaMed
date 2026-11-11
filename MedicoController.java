package com.amanda.agendamento.controller;

import com.amanda.agendamento.dto.MedicoRequest;
import com.amanda.agendamento.dto.MedicoResponse;
import com.amanda.agendamento.service.MedicoService;
import com.amanda.agendamento.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/medicos")
public class MedicoController {
    private final MedicoService service;
    private final AuthService authService;

    public MedicoController(MedicoService service, AuthService authService) { this.service = service; this.authService = authService; }

    @PostMapping
    public ResponseEntity<MedicoResponse> criar(@RequestHeader("Authorization") String authorization, @Valid @RequestBody MedicoRequest request) {
        authService.exigirAdmin(authorization);
        return ResponseEntity.status(201).body(service.salvar(request));
    }

    @GetMapping
    public ResponseEntity<List<MedicoResponse>> listar(@RequestParam(required = false) String nome) {
        return ResponseEntity.ok(service.listar(nome));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicoResponse> atualizar(@RequestHeader("Authorization") String authorization, @PathVariable Long id, @Valid @RequestBody MedicoRequest request) {
        authService.exigirAdmin(authorization);
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@RequestHeader("Authorization") String authorization, @PathVariable Long id) {
        authService.exigirAdmin(authorization);
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
