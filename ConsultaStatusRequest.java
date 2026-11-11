package com.amanda.agendamento.dto;

import jakarta.validation.constraints.NotNull;
import com.amanda.agendamento.entity.StatusConsulta;

public record ConsultaStatusRequest(
        @NotNull(message = "Status é obrigatório")
        StatusConsulta status
) {}
