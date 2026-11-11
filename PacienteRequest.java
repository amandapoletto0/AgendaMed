package com.amanda.agendamento.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PacienteRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Email(message = "E-mail inválido") String email,
        @NotBlank @Pattern(regexp = "[0-9()\\- +]{10,20}", message = "Telefone inválido") String telefone,
        @NotBlank @Pattern(regexp = "[0-9.\\-]{11,14}", message = "CPF inválido") String cpf,
        @NotNull @Past(message = "Data de nascimento deve ser anterior a hoje") LocalDate dataNascimento,
        @NotBlank @Size(max = 200) String endereco
) {}
