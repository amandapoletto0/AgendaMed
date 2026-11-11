package com.amanda.agendamento.repository;

import com.amanda.agendamento.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByTokenSessao(String tokenSessao);
    Optional<Usuario> findByPacienteId(Long pacienteId);
    boolean existsByEmail(String email);
    boolean existsByTipo(com.amanda.agendamento.entity.TipoUsuario tipo);
}
