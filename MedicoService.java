package com.amanda.agendamento.service;

import com.amanda.agendamento.dto.MedicoRequest;
import com.amanda.agendamento.dto.MedicoResponse;
import com.amanda.agendamento.entity.Medico;
import com.amanda.agendamento.exception.RegraNegocioException;
import com.amanda.agendamento.exception.RecursoNaoEncontradoException;
import com.amanda.agendamento.repository.ConsultaRepository;
import com.amanda.agendamento.repository.MedicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicoService {
    private final MedicoRepository medicoRepository;
    private final ConsultaRepository consultaRepository;

    public MedicoService(MedicoRepository medicoRepository, ConsultaRepository consultaRepository) {
        this.medicoRepository = medicoRepository;
        this.consultaRepository = consultaRepository;
    }

    @Transactional
    public MedicoResponse salvar(MedicoRequest request) {
        if (medicoRepository.existsByCrm(request.crm()))
            throw new RegraNegocioException("Já existe um médico com este CRM.");
        if (medicoRepository.existsByEmail(request.email()))
            throw new RegraNegocioException("Já existe um médico com este e-mail.");
        return toResponse(medicoRepository.save(new Medico(request.nome(), request.crm(), request.especialidade(), request.email())));
    }

    public List<MedicoResponse> listar(String nome) {
        List<Medico> medicos = nome == null || nome.isBlank() ? medicoRepository.findAll() : medicoRepository.findByNomeContainingIgnoreCase(nome);
        return medicos.stream().map(this::toResponse).toList();
    }

    public MedicoResponse buscar(Long id) {
        return toResponse(medicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico não encontrado.")));
    }

    @Transactional
    public MedicoResponse atualizar(Long id, MedicoRequest request) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico não encontrado."));
        medicoRepository.findByCrm(request.crm()).filter(m -> !m.getId().equals(id))
                .ifPresent(m -> { throw new RegraNegocioException("Já existe outro médico com este CRM."); });
        medicoRepository.findByEmail(request.email()).filter(m -> !m.getId().equals(id))
                .ifPresent(m -> { throw new RegraNegocioException("Já existe outro médico com este e-mail."); });
        medico.setNome(request.nome());
        medico.setCrm(request.crm());
        medico.setEspecialidade(request.especialidade());
        medico.setEmail(request.email());
        return toResponse(medicoRepository.save(medico));
    }

    @Transactional
    public void deletar(Long id) {
        if (!medicoRepository.existsById(id)) throw new RecursoNaoEncontradoException("Médico não encontrado.");
        if (!consultaRepository.findByMedicoId(id).isEmpty())
            throw new RegraNegocioException("Não é possível excluir um médico que possui consultas registradas.");
        medicoRepository.deleteById(id);
    }

    private MedicoResponse toResponse(Medico medico) {
        return new MedicoResponse(medico.getId(), medico.getNome(), medico.getCrm(), medico.getEspecialidade(), medico.getEmail());
    }
}
