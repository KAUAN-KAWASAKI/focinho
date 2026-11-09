package br.com.kauan.focinho.dono;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DonoRepository extends JpaRepository<Dono, Long> {

	Optional<Dono> findByEmail(String email);

	boolean existsByEmail(String email);
}
