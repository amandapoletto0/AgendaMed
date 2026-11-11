package com.amanda.agendamento.repository;

import com.amanda.agendamento.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MedicoRepository extends JpaRepository<Medico, Long> {
    Optional<Medico> findByCrm(String crm);
    Optional<Medico> findByEmail(String email);
    List<Medico> findByNomeContainingIgnoreCase(String nome);
    boolean existsByCrm(String crm);
    boolean existsByEmail(String email);
}
