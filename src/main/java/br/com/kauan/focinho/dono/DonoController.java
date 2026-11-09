package br.com.kauan.focinho.dono;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/donos")
public class DonoController {

	public record NovoDono(
			@NotBlank String nome,
			@NotBlank @Email String email,
			// 72 bytes é o limite do BCrypt
			@NotBlank @Size(min = 8, max = 72) String senha) {
	}

	public record DonoResponse(Long id, String nome, String email) {

		static DonoResponse de(Dono d) {
			return new DonoResponse(d.getId(), d.getNome(), d.getEmail());
		}
	}

	private final DonoService service;
	private final DonoRepository donos;

	public DonoController(DonoService service, DonoRepository donos) {
		this.service = service;
		this.donos = donos;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public DonoResponse cadastrar(@Valid @RequestBody NovoDono dados) {
		return DonoResponse.de(service.cadastrar(dados.nome(), dados.email(), dados.senha()));
	}

	/** Quem está logado agora. */
	@GetMapping("/eu")
	public DonoResponse eu(@AuthenticationPrincipal DonoUserDetails logado) {
		return donos.findById(logado.getDonoId()).map(DonoResponse::de)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
	}
}
