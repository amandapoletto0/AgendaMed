package com.amanda.agendamento.controller;

import com.amanda.agendamento.dto.ConsultaRequest;
import com.amanda.agendamento.dto.ConsultaResponse;
import com.amanda.agendamento.dto.ConsultaStatusRequest;
import com.amanda.agendamento.entity.StatusConsulta;
import com.amanda.agendamento.entity.TipoUsuario;
import com.amanda.agendamento.entity.Usuario;
import com.amanda.agendamento.exception.RegraNegocioException;
import com.amanda.agendamento.service.AuthService;
import com.amanda.agendamento.service.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {
    private final ConsultaService service;
    private final AuthService authService;

    public ConsultaController(ConsultaService service, AuthService authService) { this.service = service; this.authService = authService; }

    @PostMapping
    public ResponseEntity<ConsultaResponse> agendar(@RequestHeader("Authorization") String authorization, @Valid @RequestBody ConsultaRequest request) {
        Usuario usuario = authService.autenticar(authorization);
        if (usuario.getTipo() != TipoUsuario.PACIENTE || !request.pacienteId().equals(usuario.getPacienteId()))
            throw new RegraNegocioException("O agendamento deve ser feito pelo próprio paciente.");
        return ResponseEntity.status(201).body(service.agendar(request));
    }

    @GetMapping
    public ResponseEntity<List<ConsultaResponse>> listar(@RequestHeader("Authorization") String authorization) {
        Usuario usuario = authService.autenticar(authorization);
        return ResponseEntity.ok(usuario.getTipo() == TipoUsuario.ADMIN ? service.listar() : service.listarPorPaciente(usuario.getPacienteId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaResponse> buscar(@RequestHeader("Authorization") String authorization, @PathVariable Long id) {
        Usuario usuario = authService.autenticar(authorization);
        ConsultaResponse consulta = service.buscar(id);
        if (usuario.getTipo() != TipoUsuario.ADMIN && !consulta.pacienteId().equals(usuario.getPacienteId()))
            throw new RegraNegocioException("Você só pode consultar seus próprios agendamentos.");
        return ResponseEntity.ok(consulta);
    }

    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<List<ConsultaResponse>> porMedico(@RequestHeader("Authorization") String authorization, @PathVariable Long medicoId) {
        authService.exigirAdmin(authorization);
        return ResponseEntity.ok(service.listarPorMedico(medicoId));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<ConsultaResponse>> porPaciente(@RequestHeader("Authorization") String authorization, @PathVariable Long pacienteId) {
        Usuario usuario = authService.autenticar(authorization);
        if (usuario.getTipo() != TipoUsuario.ADMIN && !pacienteId.equals(usuario.getPacienteId()))
            throw new RegraNegocioException("Você só pode consultar seus próprios agendamentos.");
        return ResponseEntity.ok(service.listarPorPaciente(pacienteId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ConsultaResponse> atualizarStatus(@RequestHeader("Authorization") String authorization, @PathVariable Long id, @Valid @RequestBody ConsultaStatusRequest request) {
        Usuario usuario = authService.autenticar(authorization);
        ConsultaResponse consulta = service.buscar(id);
        if (usuario.getTipo() != TipoUsuario.ADMIN) {
            if (!consulta.pacienteId().equals(usuario.getPacienteId()) || request.status() != StatusConsulta.CANCELADA)
                throw new RegraNegocioException("O paciente só pode cancelar a própria consulta.");
        }
        return ResponseEntity.ok(service.atualizarStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@RequestHeader("Authorization") String authorization, @PathVariable Long id) {
        authService.exigirAdmin(authorization);
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
