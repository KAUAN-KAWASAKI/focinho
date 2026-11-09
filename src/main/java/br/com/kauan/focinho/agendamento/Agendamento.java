package br.com.kauan.focinho.agendamento;

import java.time.LocalDateTime;

import br.com.kauan.focinho.dono.Dono;
import br.com.kauan.focinho.servico.Servico;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Agendamento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nomePet;

	@Column(nullable = false)
	private String nomeTutor;

	@ManyToOne(optional = false)
	private Dono dono;

	@ManyToOne(optional = false)
	private Servico servico;

	@Column(nullable = false)
	private LocalDateTime inicio;

	@Column(nullable = false)
	private LocalDateTime fim;

	protected Agendamento() {
	}

	public Agendamento(Dono dono, String nomePet, String nomeTutor, Servico servico, LocalDateTime inicio) {
		this.dono = dono;
		this.nomePet = nomePet;
		this.nomeTutor = nomeTutor;
		this.servico = servico;
		this.inicio = inicio;
		this.fim = inicio.plusMinutes(servico.getDuracaoMinutos());
	}

	public Periodo periodo() {
		return new Periodo(inicio, fim);
	}

	public Long getId() {
		return id;
	}

	public Dono getDono() {
		return dono;
	}

	public String getNomePet() {
		return nomePet;
	}

	public String getNomeTutor() {
		return nomeTutor;
	}

	public Servico getServico() {
		return servico;
	}

	public LocalDateTime getInicio() {
		return inicio;
	}

	public LocalDateTime getFim() {
		return fim;
	}
}
