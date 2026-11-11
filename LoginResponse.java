package com.amanda.agendamento.dto;

public record LoginResponse(String token, String nome, String email, String tipo, Long pacienteId) {}
