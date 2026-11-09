package br.com.kauan.focinho.agendamento;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflitoDeHorarioException extends RuntimeException {

	public ConflitoDeHorarioException(String mensagem) {
		super(mensagem);
	}
}
