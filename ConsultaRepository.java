package com.amanda.agendamento.repository;

import com.amanda.agendamento.entity.Consulta;
import com.amanda.agendamento.entity.StatusConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    boolean existsByMedicoIdAndDataHoraGreaterThanAndDataHoraLessThanAndStatus(
            Long medicoId, LocalDateTime inicio, LocalDateTime fim, StatusConsulta status);
    boolean existsByPacienteIdAndDataHoraGreaterThanAndDataHoraLessThanAndStatus(
            Long pacienteId, LocalDateTime inicio, LocalDateTime fim, StatusConsulta status);
    long countByPacienteIdAndStatus(Long pacienteId, StatusConsulta status);
    List<Consulta> findByMedicoId(Long medicoId);
    List<Consulta> findByPacienteId(Long pacienteId);
    List<Consulta> findByMedicoIdAndDataHoraBetween(Long medicoId, LocalDateTime inicio, LocalDateTime fim);
}
