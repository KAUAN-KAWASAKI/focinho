package br.com.kauan.focinho.seguranca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Sobe o servidor de verdade e conversa por HTTP, do jeito que o JavaScript da tela
 * vai fazer: lê o cookie XSRF-TOKEN, devolve no cabeçalho X-XSRF-TOKEN, faz login e
 * usa a sessão. O MockMvc não serve para isso porque o csrf() dele troca o
 * repositório de token do filtro.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FluxoHttpRealTest {

	@LocalServerPort
	int porta;

	// sem seguir redirecionamentos e sem gerenciar cookies: cada cabeçalho é explícito
	private final HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

	private HttpRequest.Builder req(String caminho) {
		return HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho));
	}

	private static String valorDoCookie(HttpResponse<?> resposta, String nome) {
		return resposta.headers().allValues("set-cookie").stream().filter(c -> c.startsWith(nome + "="))
				.map(c -> c.substring(nome.length() + 1, c.indexOf(';') > 0 ? c.indexOf(';') : c.length()))
				.findFirst().orElse(null);
	}

	@Test
	void cookieDeTokenCsrfChegaAoNavegadorELegivelPeloJavaScript() throws Exception {
		HttpResponse<String> resposta = http.send(req("/api/agendamentos").GET().build(), BodyHandlers.ofString());

		assertEquals(401, resposta.statusCode());
		String cookie = resposta.headers().allValues("set-cookie").stream()
				.filter(c -> c.startsWith("XSRF-TOKEN=")).findFirst().orElseThrow();
		assertFalse(cookie.toLowerCase().contains("httponly"), "o JavaScript precisa conseguir ler este cookie");
	}

	@Test
	void cadastroLoginESessaoFuncionamPorHttp() throws Exception {
		String token = valorDoCookie(http.send(req("/api/donos/eu").GET().build(), BodyHandlers.ofString()),
				"XSRF-TOKEN");

		// sem o cabeçalho, o cadastro é barrado mesmo com o cookie
		HttpResponse<String> semCabecalho = http.send(req("/api/donos").header("Content-Type", "application/json")
				.header("Cookie", "XSRF-TOKEN=" + token).POST(BodyPublishers.ofString(corpoDono())).build(),
				BodyHandlers.ofString());
		assertEquals(403, semCabecalho.statusCode());

		// com cookie + cabeçalho iguais, passa
		HttpResponse<String> cadastro = http.send(req("/api/donos").header("Content-Type", "application/json")
				.header("Cookie", "XSRF-TOKEN=" + token).header("X-XSRF-TOKEN", token)
				.POST(BodyPublishers.ofString(corpoDono())).build(), BodyHandlers.ofString());
		assertEquals(201, cadastro.statusCode(), cadastro.body());

		// login por formulário
		String form = "username=" + URLEncoder.encode("http@petshop.com", StandardCharsets.UTF_8) + "&password="
				+ URLEncoder.encode("senha-segura-123", StandardCharsets.UTF_8);
		HttpResponse<String> login = http.send(req("/login").header("Content-Type", "application/x-www-form-urlencoded")
				.header("Cookie", "XSRF-TOKEN=" + token).header("X-XSRF-TOKEN", token)
				.POST(BodyPublishers.ofString(form)).build(), BodyHandlers.ofString());
		assertEquals(302, login.statusCode());
		assertFalse(login.headers().firstValue("location").orElse("").contains("error"));
		String sessao = valorDoCookie(login, "JSESSIONID");
		assertTrue(sessao != null && !sessao.isBlank(), "o login deveria abrir uma sessão");

		// a sessão dá acesso à API
		HttpResponse<String> eu = http.send(req("/api/donos/eu").header("Cookie", "JSESSIONID=" + sessao).GET().build(),
				BodyHandlers.ofString());
		assertEquals(200, eu.statusCode());
		assertTrue(eu.body().contains("http@petshop.com"));
	}

	private static String corpoDono() {
		return "{\"nome\":\"Http\",\"email\":\"http@petshop.com\",\"senha\":\"senha-segura-123\"}";
	}
}
