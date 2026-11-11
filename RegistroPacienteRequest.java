package com.amanda.agendamento.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RegistroPacienteRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Pattern(regexp = "[0-9.\\-]{11,14}", message = "CPF inválido") String cpf,
        @NotNull @Past(message = "Data de nascimento deve ser anterior a hoje") LocalDate dataNascimento,
        @NotBlank @Pattern(regexp = "[0-9()\\- +]{10,20}", message = "Telefone inválido") String telefone,
        @NotBlank @Size(max = 200) String endereco,
        @NotBlank @Email(message = "E-mail inválido") String email,
        @NotBlank @Size(min = 8, max = 80, message = "A senha deve ter entre 8 e 80 caracteres") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$", message = "A senha deve conter letra maiúscula, minúscula, número e caractere especial") String senha
) {}
