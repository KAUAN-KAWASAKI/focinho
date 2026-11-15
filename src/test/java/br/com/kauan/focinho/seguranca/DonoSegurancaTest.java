package br.com.kauan.focinho.seguranca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import br.com.kauan.focinho.agendamento.AgendamentoRepository;
import br.com.kauan.focinho.agendamento.RelogioFixoConfig;
import br.com.kauan.focinho.dono.Dono;
import br.com.kauan.focinho.dono.DonoRepository;
import br.com.kauan.focinho.dono.DonoUserDetails;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RelogioFixoConfig.class)
class DonoSegurancaTest {

	private static final String SENHA = "senha-segura-123";

	@Autowired
	MockMvc mvc;

	@Autowired
	DonoRepository donos;

	@Autowired
	AgendamentoRepository agendamentos;

	@Autowired
	PasswordEncoder encoder;

	@BeforeEach
	void limpar() {
		agendamentos.deleteAll();
		donos.deleteAll();
	}

	private ResultActions cadastrar(String nome, String email, String senha) throws Exception {
		return mvc.perform(post("/api/donos").with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome":"%s","email":"%s","senha":"%s"}
						""".formatted(nome, email, senha)));
	}

	private DonoUserDetails criarDono(String email) {
		return DonoUserDetails.de(donos.save(new Dono("Dono", email, encoder.encode(SENHA))));
	}

	private ResultActions agendarComo(DonoUserDetails dono, String inicio) throws Exception {
		return mvc.perform(post("/api/agendamentos").with(user(dono)).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content("""
						{"nomePet":"Rex","nomeTutor":"Ana","tipo":"BANHO","porte":"P","inicio":"%s"}
						""".formatted(inicio)));
	}

	// ---- cadastro e BCrypt ----

	@Test
	void cadastroCriaDonoSemDevolverASenha() throws Exception {
		cadastrar("Ana", "ana@petshop.com", SENHA).andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("ana@petshop.com"))
				.andExpect(jsonPath("$.senha").doesNotExist())
				.andExpect(jsonPath("$.senhaHash").doesNotExist());
	}

	@Test
	void senhaEhGuardadaComBCryptENaoEmTextoPuro() throws Exception {
		cadastrar("Ana", "ana@petshop.com", SENHA).andExpect(status().isCreated());

		String hash = donos.findByEmail("ana@petshop.com").orElseThrow().getSenhaHash();

		assertNotEquals(SENHA, hash);
		assertTrue(hash.startsWith("$2"), "deveria ser um hash BCrypt");
		assertTrue(encoder.matches(SENHA, hash));
		assertFalse(encoder.matches("outra-senha", hash));
	}

	@Test
	void emailRepetidoRetorna409MesmoComMaiusculas() throws Exception {
		cadastrar("Ana", "ana@petshop.com", SENHA).andExpect(status().isCreated());

		cadastrar("Outra Ana", "ANA@PetShop.com", SENHA).andExpect(status().isConflict());

		assertEquals(1, donos.count());
	}

	@Test
	void senhaCurtaEEmailInvalidoRetornam400() throws Exception {
		cadastrar("Ana", "ana@petshop.com", "curta").andExpect(status().isBadRequest());
		cadastrar("Ana", "isso-nao-e-email", SENHA).andExpect(status().isBadRequest());
		cadastrar("", "ana@petshop.com", SENHA).andExpect(status().isBadRequest());

		assertEquals(0, donos.count());
	}

	// ---- login ----

	@Test
	void loginComSenhaCertaAutentica() throws Exception {
		cadastrar("Ana", "ana@petshop.com", SENHA).andExpect(status().isCreated());

		mvc.perform(formLogin().user("ana@petshop.com").password(SENHA))
				.andExpect(authenticated().withUsername("ana@petshop.com"));
	}

	@Test
	void loginNaoDependeDeMaiusculasNoEmail() throws Exception {
		cadastrar("Ana", "ana@petshop.com", SENHA).andExpect(status().isCreated());

		mvc.perform(formLogin().user("ANA@PetShop.com").password(SENHA)).andExpect(authenticated());
	}

	@Test
	void loginComSenhaErradaNaoAutentica() throws Exception {
		cadastrar("Ana", "ana@petshop.com", SENHA).andExpect(status().isCreated());

		mvc.perform(formLogin().user("ana@petshop.com").password("senha-errada"))
				.andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
	}

	@Test
	void loginDeEmailInexistenteNaoAutentica() throws Exception {
		mvc.perform(formLogin().user("ninguem@petshop.com").password(SENHA)).andExpect(unauthenticated());
	}

	@Test
	void sessaoDoLoginDaAcessoAApi() throws Exception {
		cadastrar("Ana", "ana@petshop.com", SENHA).andExpect(status().isCreated());
		MockHttpSession sessao = (MockHttpSession) mvc
				.perform(formLogin().user("ana@petshop.com").password(SENHA)).andReturn().getRequest().getSession();

		mvc.perform(get("/api/donos/eu").session(sessao)).andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("ana@petshop.com"))
				.andExpect(jsonPath("$.nome").value("Ana"));
	}

	// ---- acesso ----

	@Test
	void apiSemLoginRetorna401() throws Exception {
		mvc.perform(get("/api/agendamentos")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/servicos")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/donos/eu")).andExpect(status().isUnauthorized());
	}

	// ---- CSRF ----

	@Test
	void postSemTokenCsrfRetorna403MesmoLogado() throws Exception {
		DonoUserDetails ana = criarDono("ana@petshop.com");

		mvc.perform(post("/api/agendamentos").with(user(ana)).contentType(MediaType.APPLICATION_JSON).content("""
				{"nomePet":"Rex","nomeTutor":"Ana","tipo":"BANHO","porte":"P","inicio":"2030-01-08T09:00:00"}
				""")).andExpect(status().isForbidden());

		assertEquals(0, agendamentos.count());
	}

	@Test
	void cadastroSemTokenCsrfRetorna403() throws Exception {
		mvc.perform(post("/api/donos").contentType(MediaType.APPLICATION_JSON).content("""
				{"nome":"Ana","email":"ana@petshop.com","senha":"%s"}
				""".formatted(SENHA))).andExpect(status().isForbidden());

		assertEquals(0, donos.count());
	}

	@Test
	void loginSemTokenCsrfEhRecusado() throws Exception {
		cadastrar("Ana", "ana@petshop.com", SENHA).andExpect(status().isCreated());

		mvc.perform(post("/login").param("username", "ana@petshop.com").param("password", SENHA))
				.andExpect(status().isForbidden());
	}

	// ---- agendamentos separados por dono ----

	@Test
	void umDonoNaoVeOsAgendamentosDeOutro() throws Exception {
		DonoUserDetails ana = criarDono("ana@petshop.com");
		DonoUserDetails caio = criarDono("caio@petshop.com");
		agendarComo(ana, "2030-01-08T09:00:00").andExpect(status().isCreated());

		mvc.perform(get("/api/agendamentos").with(user(caio))).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
		mvc.perform(get("/api/agendamentos").with(user(ana))).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void donosDiferentesPodemAgendarNoMesmoHorario() throws Exception {
		DonoUserDetails ana = criarDono("ana@petshop.com");
		DonoUserDetails caio = criarDono("caio@petshop.com");

		agendarComo(ana, "2030-01-08T09:00:00").andExpect(status().isCreated());
		agendarComo(caio, "2030-01-08T09:00:00").andExpect(status().isCreated());
		agendarComo(ana, "2030-01-08T09:00:00").andExpect(status().isConflict());
	}
}
