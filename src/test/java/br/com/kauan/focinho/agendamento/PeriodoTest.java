package br.com.kauan.focinho.agendamento;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Regra: conflito quando início A < fim B e fim A > início B.
 */
class PeriodoTest {

	private static Periodo periodo(String inicio, String fim) {
		LocalDate dia = LocalDate.of(2030, 1, 7);
		return new Periodo(dia.atTime(LocalTime.parse(inicio)), dia.atTime(LocalTime.parse(fim)));
	}

	@ParameterizedTest(name = "{0}: A {1}-{2} x B {3}-{4} -> conflita={5}")
	@CsvSource({
			"sobreposição parcial no fim de A,       10:00, 11:00, 10:30, 11:30, true",
			"sobreposição parcial no início de A,    10:30, 11:30, 10:00, 11:00, true",
			"B contido em A,                         09:00, 12:00, 10:00, 11:00, true",
			"A contido em B,                         10:00, 11:00, 09:00, 12:00, true",
			"períodos idênticos,                     10:00, 11:00, 10:00, 11:00, true",
			"mesmo início e B mais longo,            10:00, 11:00, 10:00, 12:00, true",
			"mesmo fim e B mais longo,               10:00, 11:00, 09:00, 11:00, true",
			"sobreposição de 1 minuto,               10:00, 11:00, 10:59, 11:30, true",
			"borda: A termina quando B começa,       09:00, 10:00, 10:00, 11:00, false",
			"borda: B termina quando A começa,       10:00, 11:00, 09:00, 10:00, false",
			"separados com folga,                    09:00, 10:00, 13:00, 14:00, false",
			"B começa 1 minuto depois de A terminar, 09:00, 10:00, 10:01, 11:00, false" })
	void conflito(String caso, String inicioA, String fimA, String inicioB, String fimB, boolean esperado) {
		Periodo a = periodo(inicioA.trim(), fimA.trim());
		Periodo b = periodo(inicioB.trim(), fimB.trim());

		assertEquals(esperado, a.conflitaCom(b));
		assertEquals(esperado, b.conflitaCom(a), "a regra deve ser simétrica");
	}

	@Test
	void diasDiferentesNaoConflitam() {
		Periodo hoje = new Periodo(LocalDateTime.of(2030, 1, 7, 10, 0), LocalDateTime.of(2030, 1, 7, 11, 0));
		Periodo amanha = new Periodo(LocalDateTime.of(2030, 1, 8, 10, 0), LocalDateTime.of(2030, 1, 8, 11, 0));

		assertEquals(false, hoje.conflitaCom(amanha));
	}
}
