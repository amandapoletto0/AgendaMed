package com.amanda.agendamento.service;

import com.amanda.agendamento.dto.PacienteRequest;
import com.amanda.agendamento.dto.PacienteResponse;
import com.amanda.agendamento.entity.Paciente;
import com.amanda.agendamento.exception.RegraNegocioException;
import com.amanda.agendamento.exception.RecursoNaoEncontradoException;
import com.amanda.agendamento.repository.ConsultaRepository;
import com.amanda.agendamento.repository.PacienteRepository;
import com.amanda.agendamento.repository.UsuarioRepository;
import com.amanda.agendamento.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PacienteService {
    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;
    private final UsuarioRepository usuarioRepository;

    public PacienteService(PacienteRepository pacienteRepository, ConsultaRepository consultaRepository, UsuarioRepository usuarioRepository) {
        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public PacienteResponse salvar(PacienteRequest request) {
        String emailNormalizado = request.email().trim().toLowerCase();
        if (pacienteRepository.existsByEmail(emailNormalizado) || usuarioRepository.existsByEmail(emailNormalizado))
            throw new RegraNegocioException("Já existe uma conta com este e-mail.");
        if (pacienteRepository.existsByCpf(request.cpf()))
            throw new RegraNegocioException("Já existe um paciente com este CPF.");
        Paciente paciente = new Paciente(request.nome(), emailNormalizado, request.telefone(), request.cpf(), request.dataNascimento(), request.endereco());
        return toResponse(pacienteRepository.save(paciente));
    }

    public List<PacienteResponse> listar(String nome) {
        List<Paciente> pacientes = nome == null || nome.isBlank() ? pacienteRepository.findAll() : pacienteRepository.findByNomeContainingIgnoreCase(nome);
        return pacientes.stream().map(this::toResponse).toList();
    }

    public PacienteResponse buscar(Long id) {
        return toResponse(pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado.")));
    }

    @Transactional
    public PacienteResponse atualizar(Long id, PacienteRequest request) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));
        String emailNormalizado = request.email().trim().toLowerCase();
        pacienteRepository.findByEmail(emailNormalizado).filter(p -> !p.getId().equals(id))
                .ifPresent(p -> { throw new RegraNegocioException("Já existe outro paciente com este e-mail."); });
        usuarioRepository.findByEmail(emailNormalizado).filter(u -> u.getPacienteId() == null || !u.getPacienteId().equals(id))
                .ifPresent(u -> { throw new RegraNegocioException("Já existe uma conta com este e-mail."); });
        paciente.setNome(request.nome());
        paciente.setEmail(emailNormalizado);
        paciente.setTelefone(request.telefone());
        pacienteRepository.findByCpf(request.cpf()).filter(p -> !p.getId().equals(id))
                .ifPresent(p -> { throw new RegraNegocioException("Já existe outro paciente com este CPF."); });
        paciente.setCpf(request.cpf());
        paciente.setDataNascimento(request.dataNascimento());
        paciente.setEndereco(request.endereco());
        Paciente salvo = pacienteRepository.save(paciente);
        usuarioRepository.findByPacienteId(id).ifPresent(usuario -> {
            usuario.setNome(salvo.getNome());
            usuario.setEmail(emailNormalizado);
            usuarioRepository.save(usuario);
        });
        return toResponse(salvo);
    }

    @Transactional
    public void deletar(Long id) {
        if (!pacienteRepository.existsById(id)) throw new RecursoNaoEncontradoException("Paciente não encontrado.");
        if (!consultaRepository.findByPacienteId(id).isEmpty())
            throw new RegraNegocioException("Não é possível excluir um paciente que possui consultas registradas.");
        pacienteRepository.deleteById(id);
    }

    private PacienteResponse toResponse(Paciente paciente) {
        return new PacienteResponse(paciente.getId(), paciente.getNome(), paciente.getEmail(), paciente.getTelefone(), paciente.getCpf(), paciente.getDataNascimento(), paciente.getEndereco(), paciente.getDataCadastro());
    }
}
