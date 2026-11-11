package com.amanda.agendamento.dto;

import java.time.LocalDate;

public record PacienteResponse(Long id, String nome, String email, String telefone, String cpf, LocalDate dataNascimento, String endereco, LocalDate dataCadastro) {}
