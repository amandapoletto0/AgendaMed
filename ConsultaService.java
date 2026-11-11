package com.amanda.agendamento.service;

import com.amanda.agendamento.dto.ConsultaRequest;
import com.amanda.agendamento.dto.ConsultaResponse;
import com.amanda.agendamento.dto.ConsultaStatusRequest;
import com.amanda.agendamento.entity.Consulta;
import com.amanda.agendamento.entity.Medico;
import com.amanda.agendamento.entity.Paciente;
import com.amanda.agendamento.entity.StatusConsulta;
import com.amanda.agendamento.exception.RegraNegocioException;
import com.amanda.agendamento.exception.RecursoNaoEncontradoException;
import com.amanda.agendamento.repository.ConsultaRepository;
import com.amanda.agendamento.repository.MedicoRepository;
import com.amanda.agendamento.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ConsultaService {
    private static final int LIMITE_CONSULTAS_ATIVAS = 3;

    private final ConsultaRepository consultaRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;

    public ConsultaService(ConsultaRepository consultaRepository, MedicoRepository medicoRepository, PacienteRepository pacienteRepository) {
        this.consultaRepository = consultaRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    @Transactional
    public ConsultaResponse agendar(ConsultaRequest request) {
        // Cada consulta dura 30 minutos. Para fechar às 19h, o último início possível é 18h30.
        LocalTime horario = request.dataHora().toLocalTime();
        if (horario.isBefore(LocalTime.of(8, 0)) || horario.isAfter(LocalTime.of(18, 30))) {
            throw new RegraNegocioException("O consultório atende das 08:00 às 19:00 e cada consulta dura 30 minutos. O último horário para iniciar é 18:30.");
        }

        Medico medico = medicoRepository.findById(request.medicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico não encontrado."));
        Paciente paciente = pacienteRepository.findById(request.pacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));

        // RN1: consultas duram 30 minutos e não podem se sobrepor para o mesmo médico.
        LocalDateTime inicioJanela = request.dataHora().minusMinutes(30);
        LocalDateTime fimJanela = request.dataHora().plusMinutes(30);
        if (consultaRepository.existsByMedicoIdAndDataHoraGreaterThanAndDataHoraLessThanAndStatus(
                medico.getId(), inicioJanela, fimJanela, StatusConsulta.AGENDADA))
            throw new RegraNegocioException("O médico já possui uma consulta que ocupa parte desse horário. Cada consulta dura 30 minutos.");

        // RN2: cada paciente pode ter no máximo 3 consultas ativas.
        if (consultaRepository.countByPacienteIdAndStatus(paciente.getId(), StatusConsulta.AGENDADA) >= LIMITE_CONSULTAS_ATIVAS)
            throw new RegraNegocioException("O paciente já possui o limite de 3 consultas ativas.");

        if (consultaRepository.existsByPacienteIdAndDataHoraGreaterThanAndDataHoraLessThanAndStatus(
                paciente.getId(), inicioJanela, fimJanela, StatusConsulta.AGENDADA))
            throw new RegraNegocioException("O paciente já possui uma consulta que ocupa parte desse horário. Cada consulta dura 30 minutos.");

        // RN3: data de criação e status inicial são gerados pelo backend.
        Consulta consulta = new Consulta(request.dataHora(), request.observacao(), medico, paciente);
        return toResponse(consultaRepository.save(consulta));
    }

    public List<ConsultaResponse> listar() {
        return consultaRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ConsultaResponse buscar(Long id) {
        return toResponse(consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada.")));
    }

    public List<ConsultaResponse> listarPorMedico(Long medicoId) {
        if (!medicoRepository.existsById(medicoId)) throw new RecursoNaoEncontradoException("Médico não encontrado.");
        return consultaRepository.findByMedicoId(medicoId).stream().map(this::toResponse).toList();
    }

    public List<ConsultaResponse> listarPorPaciente(Long pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) throw new RecursoNaoEncontradoException("Paciente não encontrado.");
        return consultaRepository.findByPacienteId(pacienteId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ConsultaResponse atualizarStatus(Long id, ConsultaStatusRequest request) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));

        // Uma consulta só pode mudar de status enquanto estiver agendada.
        // Consultas canceladas ou realizadas são finais; para remarcar, é necessário criar outra.
        if (consulta.getStatus() != StatusConsulta.AGENDADA)
            throw new RegraNegocioException("Esta consulta já foi finalizada ou cancelada e não pode ser reativada. Crie um novo agendamento.");

        if (request.status() == StatusConsulta.CANCELADA_TARDIA)
            throw new RegraNegocioException("O status de cancelamento tardio é calculado automaticamente pelo sistema.");

        if (request.status() != StatusConsulta.CANCELADA && request.status() != StatusConsulta.REALIZADA)
            throw new RegraNegocioException("A consulta agendada só pode ser cancelada ou marcada como realizada.");

        if (request.status() == StatusConsulta.REALIZADA && consulta.getDataHora().isAfter(LocalDateTime.now()))
            throw new RegraNegocioException("Uma consulta futura não pode ser marcada como realizada.");

        if (request.status() == StatusConsulta.CANCELADA) {
            LocalDateTime agora = LocalDateTime.now();
            // RN4: somente consultas futuras canceladas com menos de 24 horas
            // de antecedência recebem o status de cancelamento tardio.
            if (consulta.getDataHora().isAfter(agora)
                    && Duration.between(agora, consulta.getDataHora()).toHours() < 24) {
                consulta.setStatus(StatusConsulta.CANCELADA_TARDIA);
            } else {
                consulta.setStatus(StatusConsulta.CANCELADA);
            }
        } else {
            consulta.setStatus(StatusConsulta.REALIZADA);
        }
        return toResponse(consultaRepository.save(consulta));
    }

    @Transactional
    public void deletar(Long id) {
        if (!consultaRepository.existsById(id)) throw new RecursoNaoEncontradoException("Consulta não encontrada.");
        consultaRepository.deleteById(id);
    }

    private ConsultaResponse toResponse(Consulta consulta) {
        return new ConsultaResponse(
                consulta.getId(), consulta.getDataHora(), consulta.getDataCriacao(), consulta.getStatus(),
                consulta.getObservacao(), consulta.getMedico().getId(), consulta.getMedico().getNome(),
                consulta.getPaciente().getId(), consulta.getPaciente().getNome()
        );
    }
}
