package br.com.kauan.focinho.dono;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DonoUserDetailsService implements UserDetailsService {

	private final DonoRepository donos;

	public DonoUserDetailsService(DonoRepository donos) {
		this.donos = donos;
	}

	@Override
	public UserDetails loadUserByUsername(String email) {
		return donos.findByEmail(DonoService.normalizar(email)).map(DonoUserDetails::de)
				.orElseThrow(() -> new UsernameNotFoundException("Dono não encontrado"));
	}
}
