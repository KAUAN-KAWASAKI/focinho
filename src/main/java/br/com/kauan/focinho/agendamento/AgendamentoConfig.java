package br.com.kauan.focinho.agendamento;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HorarioFuncionamento.class)
public class AgendamentoConfig {

	/**
	 * O fuso é fixo e configurável: em servidores em UTC (como no Render) o
	 * "horário que já passou" seria calculado com horas de diferença.
	 */
	@Bean
	public Clock clock(@Value("${focinho.fuso}") ZoneId fuso) {
		return Clock.system(fuso);
	}
}
