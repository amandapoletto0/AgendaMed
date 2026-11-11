package com.amanda.agendamento.controller;

import com.amanda.agendamento.dto.PacienteRequest;
import com.amanda.agendamento.dto.PacienteResponse;
import com.amanda.agendamento.entity.TipoUsuario;
import com.amanda.agendamento.entity.Usuario;
import com.amanda.agendamento.service.AuthService;
import com.amanda.agendamento.service.PacienteService;
import com.amanda.agendamento.exception.RegraNegocioException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {
    private final PacienteService service;
    private final AuthService authService;

    public PacienteController(PacienteService service, AuthService authService) { this.service = service; this.authService = authService; }

    @PostMapping
    public ResponseEntity<PacienteResponse> criar(@RequestHeader("Authorization") String authorization, @Valid @RequestBody PacienteRequest request) {
        authService.exigirAdmin(authorization);
        return ResponseEntity.status(201).body(service.salvar(request));
    }

    @GetMapping
    public ResponseEntity<List<PacienteResponse>> listar(@RequestHeader("Authorization") String authorization, @RequestParam(required = false) String nome) {
        authService.exigirAdmin(authorization);
        return ResponseEntity.ok(service.listar(nome));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> buscar(@RequestHeader("Authorization") String authorization, @PathVariable Long id) {
        Usuario usuario = authService.autenticar(authorization);
        if (usuario.getTipo() != TipoUsuario.ADMIN && !id.equals(usuario.getPacienteId()))
            throw new RegraNegocioException("Você só pode consultar o seu próprio cadastro.");
        return ResponseEntity.ok(service.buscar(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponse> atualizar(@RequestHeader("Authorization") String authorization, @PathVariable Long id, @Valid @RequestBody PacienteRequest request) {
        Usuario usuario = authService.autenticar(authorization);
        if (usuario.getTipo() != TipoUsuario.ADMIN && !id.equals(usuario.getPacienteId()))
            throw new RegraNegocioException("Você só pode alterar o seu próprio cadastro.");
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@RequestHeader("Authorization") String authorization, @PathVariable Long id) {
        authService.exigirAdmin(authorization);
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
