package br.com.kauan.focinho.agendamento;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class AgendamentoInvalidoException extends RuntimeException {

	public AgendamentoInvalidoException(String mensagem) {
		super(mensagem);
	}
}
