package br.com.kauan.focinho.dono;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Quem está logado. Guarda só o necessário (o id do dono, não a entidade), porque
 * este objeto fica na sessão.
 */
public class DonoUserDetails implements UserDetails {

	private final Long donoId;
	private final String email;
	private final String senhaHash;

	public DonoUserDetails(Long donoId, String email, String senhaHash) {
		this.donoId = donoId;
		this.email = email;
		this.senhaHash = senhaHash;
	}

	public static DonoUserDetails de(Dono dono) {
		return new DonoUserDetails(dono.getId(), dono.getEmail(), dono.getSenhaHash());
	}

	public Long getDonoId() {
		return donoId;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_DONO"));
	}

	@Override
	public String getPassword() {
		return senhaHash;
	}

	@Override
	public String getUsername() {
		return email;
	}
}
