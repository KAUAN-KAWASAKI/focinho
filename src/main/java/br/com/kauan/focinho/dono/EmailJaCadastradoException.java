package br.com.kauan.focinho.dono;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailJaCadastradoException extends RuntimeException {

	public EmailJaCadastradoException() {
		super("Já existe um cadastro com este e-mail");
	}
}
