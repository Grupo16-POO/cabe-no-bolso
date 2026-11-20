package br.com.cabenobolso.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Participante implements Serializable, Comparable<Participante> {
    private static final long serialVersionUID = 1L;

    private final int codigo;
    private String nome;
    private BigDecimal orcamento;

    public Participante(int codigo, String nome, BigDecimal orcamento) {
        if (codigo <= 0) {
            throw new IllegalArgumentException("O código do participante deve ser maior que zero.");
        }
        this.codigo = codigo;
        atualizarDados(nome, orcamento);
    }

    public final void atualizarDados(String novoNome, BigDecimal novoOrcamento) {
        if (novoNome == null || novoNome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do participante não pode ficar vazio.");
        }
        if (novoOrcamento == null || novoOrcamento.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O orçamento deve ser maior ou igual a R$ 0,00.");
        }
        if (novoOrcamento.scale() > 2) {
            throw new IllegalArgumentException("O orçamento deve ter no máximo duas casas decimais.");
        }
        this.nome = novoNome.trim();
        this.orcamento = novoOrcamento.setScale(2, RoundingMode.UNNECESSARY);
    }

    public int getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { atualizarDados(nome, this.orcamento); }
    public BigDecimal getOrcamento() { return orcamento; }
    public void setOrcamento(BigDecimal orcamento) { atualizarDados(this.nome, orcamento); }

    @Override
    public int compareTo(Participante outro) {
        return Integer.compare(this.codigo, outro.codigo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Participante)) return false;
        return codigo == ((Participante) o).codigo;
    }

    @Override
    public int hashCode() { return Objects.hash(codigo); }

    @Override
    public String toString() { return codigo + " - " + nome; }
}