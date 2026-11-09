package br.com.kauan.focinho.agendamento;

import java.time.LocalDateTime;

/**
 * Intervalo de tempo [inicio, fim). Concentra a regra de conflito do sistema.
 */
public record Periodo(LocalDateTime inicio, LocalDateTime fim) {

	/**
	 * Dois períodos conflitam quando um começa antes de o outro terminar e termina
	 * depois de o outro começar (início A < fim B e fim A > início B). Encostar na
	 * borda (fim de um == início do outro) não é conflito.
	 */
	public boolean conflitaCom(Periodo outro) {
		return inicio.isBefore(outro.fim) && fim.isAfter(outro.inicio);
	}
}
