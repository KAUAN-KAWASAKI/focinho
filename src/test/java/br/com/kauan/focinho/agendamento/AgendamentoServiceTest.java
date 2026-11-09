package br.com.kauan.focinho.agendamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import br.com.kauan.focinho.servico.Porte;
import br.com.kauan.focinho.servico.TipoServico;

/**
 * "Agora" é segunda 2030-01-07 08:00. Expediente 08:00-18:00, de segunda a sábado.
 * Duração no catálogo: banho P 30min, banho M 45min, tosa G 90min, banho e tosa P 60min.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(RelogioFixoConfig.class)
class AgendamentoServiceTest {

	private static final LocalDateTime SEGUNDA = LocalDateTime.of(2030, 1, 7, 0, 0);
	private static final LocalDateTime TERCA = SEGUNDA.plusDays(1);
	private static final LocalDateTime DOMINGO = SEGUNDA.plusDays(6);

	@Autowired
	AgendamentoService service;

	@Autowired
	AgendamentoRepository repository;

	@BeforeEach
	void limpar() {
		repository.deleteAll();
	}

	private Agendamento banhoP(LocalDateTime inicio) {
		return service.agendar("Rex", "Ana", TipoServico.BANHO, Porte.P, inicio);
	}

	// ---- duração depende do porte ----

	@Test
	void fimEhInicioMaisDuracaoDoServicoNoPorte() {
		Agendamento tosaG = service.agendar("Thor", "Bia", TipoServico.TOSA, Porte.G, TERCA.withHour(9));

		assertEquals(TERCA.withHour(10).withMinute(30), tosaG.getFim());
	}

	@Test
	void mesmoServicoTemDuracaoDiferentePorPorte() {
		Agendamento p = service.agendar("Mel", "Ana", TipoServico.BANHO, Porte.P, TERCA.withHour(9));
		Agendamento m = service.agendar("Bob", "Caio", TipoServico.BANHO, Porte.M, TERCA.withHour(11));

		assertEquals(30, java.time.Duration.between(p.getInicio(), p.getFim()).toMinutes());
		assertEquals(45, java.time.Duration.between(m.getInicio(), m.getFim()).toMinutes());
	}

	// ---- conflito ----

	@Test
	void sobreposicaoParcialConflita() {
		service.agendar("Bob", "Caio", TipoServico.BANHO, Porte.M, TERCA.withHour(9)); // 09:00-09:45

		assertThrows(ConflitoDeHorarioException.class, () -> banhoP(TERCA.withHour(9).withMinute(30)));
	}

	@Test
	void novoDentroDeExistenteConflita() {
		service.agendar("Thor", "Bia", TipoServico.TOSA, Porte.G, TERCA.withHour(9)); // 09:00-10:30

		assertThrows(ConflitoDeHorarioException.class, () -> banhoP(TERCA.withHour(9).withMinute(30)));
	}

	@Test
	void novoQueEnvolveExistenteConflita() {
		banhoP(TERCA.withHour(10)); // 10:00-10:30

		assertThrows(ConflitoDeHorarioException.class,
				() -> service.agendar("Thor", "Bia", TipoServico.TOSA, Porte.G, TERCA.withHour(9).withMinute(30)));
	}

	@Test
	void mesmoHorarioConflita() {
		banhoP(TERCA.withHour(9));

		assertThrows(ConflitoDeHorarioException.class, () -> banhoP(TERCA.withHour(9)));
	}

	@Test
	void comecarExatamenteQuandoOutroTerminaNaoConflita() {
		banhoP(TERCA.withHour(9)); // 09:00-09:30

		Agendamento seguinte = banhoP(TERCA.withHour(9).withMinute(30));

		assertEquals(TERCA.withHour(10), seguinte.getFim());
	}

	@Test
	void terminarExatamenteQuandoOutroComecaNaoConflita() {
		banhoP(TERCA.withHour(9).withMinute(30)); // 09:30-10:00

		Agendamento anterior = banhoP(TERCA.withHour(9)); // 09:00-09:30

		assertEquals(TERCA.withHour(9).withMinute(30), anterior.getFim());
	}

	@Test
	void mesmoHorarioEmDiasDiferentesNaoConflita() {
		banhoP(TERCA.withHour(9));

		banhoP(TERCA.plusDays(1).withHour(9));

		assertEquals(2, service.listar().size());
	}

	@Test
	void agendamentoComConflitoNaoEhGravado() {
		banhoP(TERCA.withHour(9));

		assertThrows(ConflitoDeHorarioException.class, () -> banhoP(TERCA.withHour(9)));

		assertEquals(1, service.listar().size());
	}

	// ---- horário passado ----

	@Test
	void diaAnteriorEhRejeitado() {
		AgendamentoInvalidoException erro = assertThrows(AgendamentoInvalidoException.class,
				() -> banhoP(LocalDateTime.of(2030, 1, 4, 9, 0)));

		assertTrue(erro.getMessage().contains("passou"));
	}

	@Test
	void comecarExatamenteAgoraEhPermitido() {
		Agendamento agendamento = banhoP(RelogioFixoConfig.AGORA);

		assertEquals(RelogioFixoConfig.AGORA, agendamento.getInicio());
	}

	@Test
	void umMinutoNoPassadoEhRejeitado() {
		assertThrows(AgendamentoInvalidoException.class, () -> banhoP(RelogioFixoConfig.AGORA.minusMinutes(1)));
	}

	// ---- horário de funcionamento ----

	@Test
	void comecarExatamenteNaAberturaEhPermitido() {
		banhoP(TERCA.withHour(8));
	}

	@Test
	void comecarUmMinutoAntesDaAberturaEhRejeitado() {
		assertThrows(AgendamentoInvalidoException.class, () -> banhoP(TERCA.withHour(7).withMinute(59)));
	}

	@Test
	void terminarExatamenteNoFechamentoEhPermitido() {
		banhoP(TERCA.withHour(17).withMinute(30)); // 17:30-18:00
	}

	@Test
	void terminarUmMinutoDepoisDoFechamentoEhRejeitado() {
		assertThrows(AgendamentoInvalidoException.class, () -> banhoP(TERCA.withHour(17).withMinute(31)));
	}

	@Test
	void comecarNoFechamentoEhRejeitado() {
		assertThrows(AgendamentoInvalidoException.class, () -> banhoP(TERCA.withHour(18)));
	}

	@Test
	void servicoLongoQueUltrapassaOFechamentoEhRejeitado() {
		assertThrows(AgendamentoInvalidoException.class,
				() -> service.agendar("Thor", "Bia", TipoServico.TOSA, Porte.G, TERCA.withHour(17)));
	}

	@Test
	void domingoEhRejeitado() {
		assertThrows(AgendamentoInvalidoException.class, () -> banhoP(DOMINGO.withHour(10)));
	}
}
