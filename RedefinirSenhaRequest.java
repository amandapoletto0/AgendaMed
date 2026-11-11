package com.amanda.agendamento.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(
        @NotBlank @Email(message = "E-mail inválido") String email,
        @NotBlank @Pattern(regexp = "[0-9.\\-]{11,14}", message = "Informe um CPF válido") String cpf,
        @NotBlank @Size(min = 8, max = 80, message = "A senha deve ter entre 8 e 80 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$", message = "A senha deve conter letra maiúscula, minúscula, número e caractere especial") String novaSenha
) {}
