package com.amanda.agendamento.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/** Centraliza a criação e a verificação de hashes de senha. */
@Service
public class SenhaService {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String criptografar(String senha) {
        return encoder.encode(senha);
    }

    public boolean corresponde(String senhaDigitada, String senhaArmazenada) {
        if (senhaArmazenada == null) return false;
        // Compatibilidade temporária com contas criadas antes da adoção do BCrypt.
        if (!senhaArmazenada.startsWith("$2a$") && !senhaArmazenada.startsWith("$2b$") && !senhaArmazenada.startsWith("$2y$")) {
            return senhaArmazenada.equals(senhaDigitada);
        }
        return encoder.matches(senhaDigitada, senhaArmazenada);
    }

    public boolean estaCriptografada(String senha) {
        return senha != null && (senha.startsWith("$2a$") || senha.startsWith("$2b$") || senha.startsWith("$2y$"));
    }
}
