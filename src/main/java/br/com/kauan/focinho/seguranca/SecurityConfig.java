package br.com.kauan.focinho.seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filtros(HttpSecurity http) throws Exception {
		http
				// CSRF continua ligado em tudo. O token fica num cookie legível pelo
				// JavaScript (XSRF-TOKEN) e volta no cabeçalho X-XSRF-TOKEN.
				.csrf(csrf -> csrf
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
						.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
				.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/donos").permitAll()
						.requestMatchers("/login", "/error").permitAll()
						.requestMatchers("/api/**").authenticated()
						.anyRequest().permitAll())
				// A API responde 401 em vez de redirecionar para a tela de login.
				.exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(
						new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
						PathPatternRequestMatcher.withDefaults().matcher("/api/**")))
				.formLogin(Customizer.withDefaults())
				.logout(Customizer.withDefaults());
		return http.build();
	}
}
