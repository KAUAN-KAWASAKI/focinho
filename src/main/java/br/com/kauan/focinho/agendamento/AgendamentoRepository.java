package br.com.kauan.focinho.agendamento;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

	List<Agendamento> findByInicioGreaterThanEqualAndInicioLessThan(LocalDateTime de, LocalDateTime ate);

	List<Agendamento> findAllByOrderByInicioAsc();
}
