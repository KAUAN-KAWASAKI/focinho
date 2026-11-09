package br.com.kauan.focinho.servico;

public enum TipoServico {
	BANHO("Banho"),
	TOSA("Tosa"),
	BANHO_E_TOSA("Banho e tosa");

	private final String descricao;

	TipoServico(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}
