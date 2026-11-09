package br.com.kauan.focinho.servico;

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

	private final ServicoRepository repository;

	public ServicoController(ServicoRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public List<Servico> listar() {
		return repository.findAllByOrderByTipoAscPorteAsc();
	}

	@GetMapping("/{tipo}/{porte}")
	public Servico buscar(@PathVariable TipoServico tipo, @PathVariable Porte porte) {
		return repository.findByTipoAndPorte(tipo, porte)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));
	}
}
