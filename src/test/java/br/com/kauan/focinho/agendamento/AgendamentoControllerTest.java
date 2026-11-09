package br.com.kauan.focinho.agendamento;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RelogioFixoConfig.class)
class AgendamentoControllerTest {

	@Autowired
	MockMvc mvc;

	@Autowired
	AgendamentoRepository repository;

	@BeforeEach
	void limpar() {
		repository.deleteAll();
	}

	private static String corpo(String nomePet, String tipo, String porte, String inicio) {
		return """
				{"nomePet":"%s","nomeTutor":"Ana","tipo":"%s","porte":"%s","inicio":"%s"}
				""".formatted(nomePet, tipo, porte, inicio);
	}

	private org.springframework.test.web.servlet.ResultActions agendar(String json) throws Exception {
		return mvc.perform(post("/api/agendamentos").contentType(MediaType.APPLICATION_JSON).content(json));
	}

	@Test
	void criaAgendamentoComFimCalculadoPelaDuracao() throws Exception {
		agendar(corpo("Thor", "TOSA", "G", "2030-01-08T09:00:00")).andExpect(status().isCreated())
				.andExpect(jsonPath("$.inicio").value("2030-01-08T09:00:00"))
				.andExpect(jsonPath("$.fim").value("2030-01-08T10:30:00"));
	}

	@Test
	void sobreposicaoRetorna409() throws Exception {
		agendar(corpo("Rex", "BANHO", "P", "2030-01-08T09:00:00")).andExpect(status().isCreated());

		agendar(corpo("Mel", "BANHO", "P", "2030-01-08T09:15:00")).andExpect(status().isConflict());
	}

	@Test
	void horarioEncostadoRetorna201() throws Exception {
		agendar(corpo("Rex", "BANHO", "P", "2030-01-08T09:00:00")).andExpect(status().isCreated());

		agendar(corpo("Mel", "BANHO", "P", "2030-01-08T09:30:00")).andExpect(status().isCreated());
	}

	@Test
	void horarioPassadoRetorna422() throws Exception {
		agendar(corpo("Rex", "BANHO", "P", "2030-01-04T09:00:00")).andExpect(status().isUnprocessableEntity());
	}

	@Test
	void foraDoExpedienteRetorna422() throws Exception {
		agendar(corpo("Rex", "BANHO", "P", "2030-01-08T18:00:00")).andExpect(status().isUnprocessableEntity());
	}

	@Test
	void dadosFaltandoRetorna400() throws Exception {
		agendar(corpo("", "BANHO", "P", "2030-01-08T09:00:00")).andExpect(status().isBadRequest());
	}

	@Test
	void listaOsAgendamentosEmOrdemDeInicio() throws Exception {
		agendar(corpo("Segundo", "BANHO", "P", "2030-01-08T11:00:00")).andExpect(status().isCreated());
		agendar(corpo("Primeiro", "BANHO", "P", "2030-01-08T09:00:00")).andExpect(status().isCreated());

		mvc.perform(get("/api/agendamentos")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].nomePet").value("Primeiro"))
				.andExpect(jsonPath("$[1].nomePet").value("Segundo"));
	}

	@Test
	void catalogoTemNoveServicosComDuracaoPorPorte() throws Exception {
		mvc.perform(get("/api/servicos")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(9));
		mvc.perform(get("/api/servicos/TOSA/G")).andExpect(status().isOk())
				.andExpect(jsonPath("$.duracaoMinutos").value(90))
				.andExpect(jsonPath("$.descricao").value("Tosa"));
	}
}
