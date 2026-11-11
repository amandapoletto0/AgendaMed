package com.amanda.agendamento;

import com.amanda.agendamento.entity.Medico;
import com.amanda.agendamento.entity.TipoUsuario;
import com.amanda.agendamento.entity.Usuario;
import com.amanda.agendamento.repository.MedicoRepository;
import com.amanda.agendamento.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.amanda.agendamento.service.SenhaService;

@SpringBootApplication
public class AgendamentoApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgendamentoApplication.class, args);
    }

    @Bean
    CommandLineRunner dadosIniciais(UsuarioRepository usuarioRepository, MedicoRepository medicoRepository, SenhaService senhaService) {
        return args -> {
            if (!usuarioRepository.existsByTipo(TipoUsuario.ADMIN)) {
                Usuario admin = new Usuario("Administrador", "admin@agendamed.com", senhaService.criptografar("admin123"), TipoUsuario.ADMIN, null);
                usuarioRepository.save(admin);
            }
            if (medicoRepository.count() == 0) {
                medicoRepository.save(new Medico("Dra. Ana Martins", "123456", "Clínica Geral", "ana.martins@agendamed.com"));
            }
        };
    }
}
