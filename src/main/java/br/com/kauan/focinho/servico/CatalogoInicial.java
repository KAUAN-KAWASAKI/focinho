package br.com.kauan.focinho.servico;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Cadastra o catálogo padrão (duração e preço por tipo e porte) na primeira execução.
 */
@Component
public class CatalogoInicial implements CommandLineRunner {

	private final ServicoRepository repository;

	public CatalogoInicial(ServicoRepository repository) {
		this.repository = repository;
	}

	@Override
	public void run(String... args) {
		if (repository.count() > 0) {
			return;
		}
		cadastrar(TipoServico.BANHO, new int[] { 30, 45, 60 }, new String[] { "40.00", "55.00", "70.00" });
		cadastrar(TipoServico.TOSA, new int[] { 45, 60, 90 }, new String[] { "50.00", "65.00", "85.00" });
		cadastrar(TipoServico.BANHO_E_TOSA, new int[] { 60, 90, 120 }, new String[] { "80.00", "105.00", "135.00" });
	}

	private void cadastrar(TipoServico tipo, int[] duracoes, String[] precos) {
		Porte[] portes = Porte.values();
		for (int i = 0; i < portes.length; i++) {
			repository.save(new Servico(tipo, portes[i], duracoes[i], new BigDecimal(precos[i])));
		}
	}
}
