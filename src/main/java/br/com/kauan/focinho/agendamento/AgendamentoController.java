package br.com.kauan.focinho.agendamento;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.kauan.focinho.dono.DonoUserDetails;
import br.com.kauan.focinho.servico.Porte;
import br.com.kauan.focinho.servico.TipoServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

	public record NovoAgendamento(
			@NotBlank String nomePet,
			@NotBlank String nomeTutor,
			@NotNull TipoServico tipo,
			@NotNull Porte porte,
			@NotNull LocalDateTime inicio) {
	}

	public record AgendamentoResponse(Long id, String nomePet, String nomeTutor, TipoServico tipo, Porte porte,
			LocalDateTime inicio, LocalDateTime fim) {

		static AgendamentoResponse de(Agendamento a) {
			return new AgendamentoResponse(a.getId(), a.getNomePet(), a.getNomeTutor(), a.getServico().getTipo(),
					a.getServico().getPorte(), a.getInicio(), a.getFim());
		}
	}

	private final AgendamentoService service;

	public AgendamentoController(AgendamentoService service) {
		this.service = service;
	}

	@GetMapping
	public List<AgendamentoResponse> listar(@AuthenticationPrincipal DonoUserDetails dono) {
		return service.listar(dono.getDonoId()).stream().map(AgendamentoResponse::de).toList();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AgendamentoResponse agendar(@AuthenticationPrincipal DonoUserDetails dono,
			@Valid @RequestBody NovoAgendamento dados) {
		return AgendamentoResponse.de(service.agendar(dono.getDonoId(), dados.nomePet(), dados.nomeTutor(),
				dados.tipo(), dados.porte(), dados.inicio()));
	}
}
