package br.com.kauan.focinho.servico;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

	Optional<Servico> findByTipoAndPorte(TipoServico tipo, Porte porte);

	List<Servico> findAllByOrderByTipoAscPorteAsc();
}
