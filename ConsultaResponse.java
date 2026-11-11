package com.amanda.agendamento.dto;

import com.amanda.agendamento.entity.StatusConsulta;
import java.time.LocalDateTime;

public record ConsultaResponse(
        Long id,
        LocalDateTime dataHora,
        LocalDateTime dataCriacao,
        StatusConsulta status,
        String observacao,
        Long medicoId,
        String medicoNome,
        Long pacienteId,
        String pacienteNome
) {}
