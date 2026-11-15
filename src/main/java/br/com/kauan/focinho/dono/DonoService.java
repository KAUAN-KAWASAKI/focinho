package br.com.kauan.focinho.dono;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DonoService {

	private final DonoRepository donos;
	private final PasswordEncoder encoder;

	public DonoService(DonoRepository donos, PasswordEncoder encoder) {
		this.donos = donos;
		this.encoder = encoder;
	}

	static String normalizar(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	public Dono cadastrar(String nome, String email, String senha) {
		String emailNormalizado = normalizar(email);
		if (donos.existsByEmail(emailNormalizado)) {
			throw new EmailJaCadastradoException();
		}
		try {
			return donos.save(new Dono(nome.trim(), emailNormalizado, encoder.encode(senha)));
		} catch (DataIntegrityViolationException e) {
			// duas requisições com o mesmo e-mail ao mesmo tempo: a constraint única decide
			throw new EmailJaCadastradoException();
		}
	}
}
