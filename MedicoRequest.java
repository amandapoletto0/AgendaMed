package com.amanda.agendamento.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicoRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,
        @NotBlank(message = "CRM é obrigatório")
        @Size(max = 30, message = "CRM deve ter no máximo 30 caracteres")
        String crm,
        @NotBlank(message = "Especialidade é obrigatória")
        @Size(max = 80, message = "Especialidade deve ter no máximo 80 caracteres")
        String especialidade,
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email
) {}
