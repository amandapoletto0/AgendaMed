package com.amanda.agendamento.repository;

import com.amanda.agendamento.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByEmail(String email);
    List<Paciente> findByNomeContainingIgnoreCase(String nome);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    java.util.Optional<Paciente> findByCpf(String cpf);
}
