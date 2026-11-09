package br.com.kauan.focinho.agendamento;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Congela "agora" em segunda-feira, 2030-01-07 às 08:00 (exatamente na abertura),
 * para os testes não dependerem da data em que rodam.
 */
@TestConfiguration
public class RelogioFixoConfig {

	static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
	static final LocalDateTime AGORA = LocalDateTime.of(2030, 1, 7, 8, 0);

	@Bean
	@Primary
	Clock relogioFixo() {
		return Clock.fixed(AGORA.atZone(FUSO).toInstant(), FUSO);
	}
}
