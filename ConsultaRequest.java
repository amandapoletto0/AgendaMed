package com.amanda.agendamento.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record ConsultaRequest(
        @NotNull(message = "Data e hora da consulta são obrigatórias")
        @Future(message = "A consulta deve ser agendada para uma data futura")
        LocalDateTime dataHora,
        @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
        String observacao,
        @NotNull(message = "ID do médico é obrigatório")
        Long medicoId,
        @NotNull(message = "ID do paciente é obrigatório")
        Long pacienteId
) {}
