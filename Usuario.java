package com.amanda.agendamento.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipo;

    @Column(unique = true)
    private String tokenSessao;

    private Long pacienteId;

    public Usuario() {}

    public Usuario(String nome, String email, String senha, TipoUsuario tipo, Long pacienteId) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
        this.pacienteId = pacienteId;
    }

    public void gerarToken() { this.tokenSessao = UUID.randomUUID().toString(); }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public TipoUsuario getTipo() { return tipo; }
    public String getTokenSessao() { return tokenSessao; }
    public Long getPacienteId() { return pacienteId; }
    public void setSenha(String senha) { this.senha = senha; }
    public void setNome(String nome) { this.nome = nome; }
    public void setEmail(String email) { this.email = email; }
    public void setTokenSessao(String tokenSessao) { this.tokenSessao = tokenSessao; }
}
