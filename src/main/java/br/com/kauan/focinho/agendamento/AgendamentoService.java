package br.com.kauan.focinho.agendamento;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.kauan.focinho.dono.DonoRepository;
import br.com.kauan.focinho.servico.Porte;
import br.com.kauan.focinho.servico.Servico;
import br.com.kauan.focinho.servico.ServicoRepository;
import br.com.kauan.focinho.servico.TipoServico;

@Service
public class AgendamentoService {

	private final AgendamentoRepository agendamentos;
	private final ServicoRepository servicos;
	private final DonoRepository donos;
	private final HorarioFuncionamento funcionamento;
	private final Clock clock;

	public AgendamentoService(AgendamentoRepository agendamentos, ServicoRepository servicos, DonoRepository donos,
			HorarioFuncionamento funcionamento, Clock clock) {
		this.agendamentos = agendamentos;
		this.servicos = servicos;
		this.donos = donos;
		this.funcionamento = funcionamento;
		this.clock = clock;
	}

	/** Só os agendamentos do dono informado. */
	public List<Agendamento> listar(Long donoId) {
		return agendamentos.findByDonoIdOrderByInicioAsc(donoId);
	}

	/**
	 * Não é transacional de propósito: o save confirma na hora, e o synchronized
	 * impede que duas requisições passem pela checagem de conflito ao mesmo tempo
	 * (vale para uma única instância da aplicação).
	 */
	public synchronized Agendamento agendar(Long donoId, String nomePet, String nomeTutor, TipoServico tipo,
			Porte porte, LocalDateTime inicio) {
		Servico servico = servicos.findByTipoAndPorte(tipo, porte)
				.orElseThrow(() -> new AgendamentoInvalidoException("Serviço não cadastrado para este porte"));
		Agendamento novo = new Agendamento(donos.getReferenceById(donoId), nomePet, nomeTutor, servico, inicio);

		validarHorario(novo.periodo());
		validarConflito(donoId, novo.periodo());

		return agendamentos.save(novo);
	}

	private void validarHorario(Periodo periodo) {
		if (periodo.inicio().isBefore(LocalDateTime.now(clock))) {
			throw new AgendamentoInvalidoException("Não é possível agendar em um horário que já passou");
		}
		if (!funcionamento.dias().contains(periodo.inicio().getDayOfWeek())) {
			throw new AgendamentoInvalidoException("O pet shop não funciona neste dia da semana");
		}
		boolean mesmoDia = periodo.fim().toLocalDate().equals(periodo.inicio().toLocalDate());
		boolean dentroDoExpediente = !periodo.inicio().toLocalTime().isBefore(funcionamento.abertura())
				&& !periodo.fim().toLocalTime().isAfter(funcionamento.fechamento());
		if (!mesmoDia || !dentroDoExpediente) {
			throw new AgendamentoInvalidoException("O atendimento precisa começar e terminar entre "
					+ funcionamento.abertura() + " e " + funcionamento.fechamento());
		}
	}

	/** O conflito vale dentro da agenda do dono: pet shops diferentes não se bloqueiam. */
	private void validarConflito(Long donoId, Periodo novo) {
		LocalDate dia = novo.inicio().toLocalDate();
		boolean conflita = agendamentos
				.findByDonoIdAndInicioGreaterThanEqualAndInicioLessThan(donoId, dia.atStartOfDay(),
						dia.plusDays(1).atStartOfDay())
				.stream()
				.anyMatch(existente -> novo.conflitaCom(existente.periodo()));
		if (conflita) {
			throw new ConflitoDeHorarioException("Já existe um agendamento que se sobrepõe a este horário");
		}
	}
}
