package br.com.kauan.focinho.servico;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Um tipo de serviço para um porte: cada combinação tem sua duração e seu preço.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "tipo", "porte" }))
public class Servico {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoServico tipo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Porte porte;

	@Column(nullable = false)
	private int duracaoMinutos;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal preco;

	protected Servico() {
	}

	public Servico(TipoServico tipo, Porte porte, int duracaoMinutos, BigDecimal preco) {
		this.tipo = tipo;
		this.porte = porte;
		this.duracaoMinutos = duracaoMinutos;
		this.preco = preco;
	}

	public Long getId() {
		return id;
	}

	public TipoServico getTipo() {
		return tipo;
	}

	public Porte getPorte() {
		return porte;
	}

	public int getDuracaoMinutos() {
		return duracaoMinutos;
	}

	public BigDecimal getPreco() {
		return preco;
	}
}
