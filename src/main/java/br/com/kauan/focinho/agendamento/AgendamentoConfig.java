package br.com.kauan.focinho.agendamento;

import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HorarioFuncionamento.class)
public class AgendamentoConfig {

	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}
}
