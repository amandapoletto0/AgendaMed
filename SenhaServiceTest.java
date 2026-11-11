package com.amanda.agendamento.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SenhaServiceTest {
    private final SenhaService senhaService = new SenhaService();

    @Test
    void deveArmazenarHashEmVezDaSenhaOriginal() {
        String senha = "AgendaMed@2026";
        String hash = senhaService.criptografar(senha);

        assertNotEquals(senha, hash);
        assertTrue(senhaService.estaCriptografada(hash));
        assertTrue(senhaService.corresponde(senha, hash));
    }

    @Test
    void deveRejeitarSenhaIncorreta() {
        String hash = senhaService.criptografar("AgendaMed@2026");

        assertFalse(senhaService.corresponde("SenhaErrada@1", hash));
    }

    @Test
    void deveAceitarTemporariamenteSenhaLegadaParaMigracaoNoLogin() {
        assertTrue(senhaService.corresponde("admin123", "admin123"));
        assertFalse(senhaService.estaCriptografada("admin123"));
    }
}
