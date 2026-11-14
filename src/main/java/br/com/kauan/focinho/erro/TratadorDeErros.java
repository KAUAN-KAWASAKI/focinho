package br.com.kauan.focinho.erro;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.kauan.focinho.agendamento.AgendamentoInvalidoException;
import br.com.kauan.focinho.agendamento.ConflitoDeHorarioException;
import br.com.kauan.focinho.dono.EmailJaCadastradoException;

/**
 * Transforma as regras de negócio em respostas {"message": "..."}, que a tela mostra
 * ao usuário. Sem isso o Spring devolve só o status, sem dizer o motivo.
 */
@RestControllerAdvice
public class TratadorDeErros {

	@ExceptionHandler(ConflitoDeHorarioException.class)
	ResponseEntity<Map<String, String>> conflito(ConflitoDeHorarioException e) {
		return resposta(HttpStatus.CONFLICT, e.getMessage());
	}

	@ExceptionHandler(EmailJaCadastradoException.class)
	ResponseEntity<Map<String, String>> emailJaCadastrado(EmailJaCadastradoException e) {
		return resposta(HttpStatus.CONFLICT, e.getMessage());
	}

	@ExceptionHandler(AgendamentoInvalidoException.class)
	ResponseEntity<Map<String, String>> agendamentoInvalido(AgendamentoInvalidoException e) {
		return resposta(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<Map<String, String>> camposInvalidos(MethodArgumentNotValidException e) {
		return resposta(HttpStatus.BAD_REQUEST, "Confira os campos: algum valor está vazio ou inválido.");
	}

	private static ResponseEntity<Map<String, String>> resposta(HttpStatus status, String mensagem) {
		return ResponseEntity.status(status).body(Map.of("message", mensagem));
	}
}
