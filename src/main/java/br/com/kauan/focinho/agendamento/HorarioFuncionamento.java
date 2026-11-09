package br.com.kauan.focinho.agendamento;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("focinho.funcionamento")
public record HorarioFuncionamento(LocalTime abertura, LocalTime fechamento, Set<DayOfWeek> dias) {
}
