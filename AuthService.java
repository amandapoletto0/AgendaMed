package com.amanda.agendamento.service;

import com.amanda.agendamento.dto.*;
import com.amanda.agendamento.entity.Paciente;
import com.amanda.agendamento.entity.TipoUsuario;
import com.amanda.agendamento.entity.Usuario;
import com.amanda.agendamento.exception.RegraNegocioException;
import com.amanda.agendamento.repository.PacienteRepository;
import com.amanda.agendamento.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final SenhaService senhaService;

    public AuthService(UsuarioRepository usuarioRepository, PacienteRepository pacienteRepository, SenhaService senhaService) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.senhaService = senhaService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new RegraNegocioException("E-mail ou senha incorretos."));
        if (!senhaService.corresponde(request.senha(), usuario.getSenha()))
            throw new RegraNegocioException("E-mail ou senha incorretos.");
        if (!senhaService.estaCriptografada(usuario.getSenha())) {
            usuario.setSenha(senhaService.criptografar(request.senha()));
        }
        usuario.gerarToken();
        usuarioRepository.save(usuario);
        return new LoginResponse(usuario.getTokenSessao(), usuario.getNome(), usuario.getEmail(), usuario.getTipo().name(), usuario.getPacienteId());
    }

    @Transactional
    public LoginResponse registrarPaciente(RegistroPacienteRequest request) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email) || pacienteRepository.existsByEmail(email))
            throw new RegraNegocioException("Já existe uma conta com este e-mail.");
        if (pacienteRepository.existsByCpf(request.cpf()))
            throw new RegraNegocioException("Já existe um paciente com este CPF.");
        Paciente paciente = pacienteRepository.save(new Paciente(
                request.nome(), email, request.telefone(), request.cpf(), request.dataNascimento(), request.endereco()));
        Usuario usuario = new Usuario(paciente.getNome(), email, senhaService.criptografar(request.senha()), TipoUsuario.PACIENTE, paciente.getId());
        usuario.gerarToken();
        usuarioRepository.save(usuario);
        return new LoginResponse(usuario.getTokenSessao(), usuario.getNome(), usuario.getEmail(), usuario.getTipo().name(), paciente.getId());
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new RegraNegocioException("Não existe conta cadastrada com este e-mail."));
        if (usuario.getTipo() != TipoUsuario.PACIENTE || usuario.getPacienteId() == null) {
            throw new RegraNegocioException("A recuperação automática está disponível apenas para contas de pacientes.");
        }
        Paciente paciente = pacienteRepository.findById(usuario.getPacienteId())
                .orElseThrow(() -> new RegraNegocioException("Cadastro de paciente não encontrado."));
        if (!paciente.getCpf().equals(request.cpf())) {
            throw new RegraNegocioException("E-mail ou CPF não conferem.");
        }
        usuario.setSenha(senhaService.criptografar(request.novaSenha()));
        usuario.setTokenSessao(null);
        usuarioRepository.save(usuario);
    }

    public Usuario autenticar(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new RegraNegocioException("É necessário estar logado.");
        return usuarioRepository.findByTokenSessao(authorization.substring(7))
                .orElseThrow(() -> new RegraNegocioException("Sessão inválida ou expirada."));
    }

    public Usuario exigirAdmin(String authorization) {
        Usuario usuario = autenticar(authorization);
        if (usuario.getTipo() != TipoUsuario.ADMIN)
            throw new RegraNegocioException("Apenas o administrador pode gerenciar os médicos.");
        return usuario;
    }
}
