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
            throw new IllegalArgumentException("O código do participante deve ser um número inteiro positivo.");
        }
        String nomeValidado = validarNome(nome);
        BigDecimal orcamentoValidado = validarOrcamento(orcamento);

        this.codigo = codigo;
        this.nome = nomeValidado;
        this.orcamento = orcamentoValidado;
    }

    /**
     * Atualiza nome e orçamento de forma atômica: se um falhar, nenhum é alterado.
     */
    public void atualizarDados(String novoNome, BigDecimal novoOrcamento) {
        String nomeValidado = validarNome(novoNome);
        BigDecimal orcamentoValidado = validarOrcamento(novoOrcamento);

        this.nome = nomeValidado;
        this.orcamento = orcamentoValidado;
    }

    private String validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do participante não pode ficar vazio.");
        }
        return nome.trim();
    }

    private BigDecimal validarOrcamento(BigDecimal orcamento) {
        if (orcamento == null || orcamento.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O orçamento deve ser maior ou igual a R$ 0,00.");
        }
        if (orcamento.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("O orçamento deve ter no máximo duas casas decimais.");
        }
        return orcamento.setScale(2, RoundingMode.UNNECESSARY);
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = validarNome(nome);
    }

    public BigDecimal getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(BigDecimal orcamento) {
        this.orcamento = validarOrcamento(orcamento);
    }

    @Override
    public int compareTo(Participante outro) {
        return Integer.compare(this.codigo, outro.codigo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participante that = (Participante) o;
        return codigo == that.codigo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return codigo + " - " + nome;
    }
}