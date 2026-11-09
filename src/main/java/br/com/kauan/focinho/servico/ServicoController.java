package br.com.kauan.focinho.servico;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

	public record ServicoResponse(TipoServico tipo, String descricao, Porte porte, int duracaoMinutos,
			BigDecimal preco) {

		static ServicoResponse de(Servico s) {
			return new ServicoResponse(s.getTipo(), s.getTipo().getDescricao(), s.getPorte(), s.getDuracaoMinutos(),
					s.getPreco());
		}
	}

	private final ServicoRepository repository;

	public ServicoController(ServicoRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public List<ServicoResponse> listar() {
		return repository.findAllByOrderByTipoAscPorteAsc().stream().map(ServicoResponse::de).toList();
	}

	@GetMapping("/{tipo}/{porte}")
	public ServicoResponse buscar(@PathVariable TipoServico tipo, @PathVariable Porte porte) {
		return repository.findByTipoAndPorte(tipo, porte).map(ServicoResponse::de)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));
	}
}
