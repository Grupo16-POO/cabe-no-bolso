package br.com.cabenobolso.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class Despesa implements Serializable {
    private static final long serialVersionUID = 1L;

    private String descricao;
    private BigDecimal valor;
    private Set<Participante> participantes;
    private boolean essencial;
    private int prioridade;

    public Despesa(String descricao, BigDecimal valor, Collection<Participante> participantes,
                   boolean essencial, int prioridade) {
        atualizarDados(descricao, valor, participantes, essencial, prioridade);
    }

    /**
     * Atualiza todos os atributos de forma atômica, preservando o estado anterior em caso de erro.
     */
    public final void atualizarDados(String novaDescricao, BigDecimal novoValor,
                                     Collection<Participante> novosParticipantes,
                                     boolean essencial, int prioridade) {
        String descValidada = validarDescricao(novaDescricao);
        BigDecimal valorValidado = validarValor(novoValor);
        Set<Participante> partValidados = validarParticipantes(novosParticipantes);
        int prioridadeValidada = validarPrioridade(essencial, prioridade);

        this.descricao = descValidada;
        this.valor = valorValidado;
        this.participantes = partValidados;
        this.essencial = essencial;
        this.prioridade = prioridadeValidada;
    }

    private String validarDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("A descrição da despesa não pode ficar vazia.");
        }
        return descricao.trim();
    }

    private BigDecimal validarValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da despesa deve ser maior que R$ 0,00.");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("O valor da despesa deve ter no máximo duas casas decimais.");
        }
        return valor.setScale(2, RoundingMode.UNNECESSARY);
    }

    private Set<Participante> validarParticipantes(Collection<Participante> lista) {
        if (lista == null || lista.isEmpty()) {
            throw new IllegalArgumentException("A despesa deve ter pelo menos um participante selecionado.");
        }
        for (Participante p : lista) {
            if (p == null) {
                throw new IllegalArgumentException("Participante inválido na lista da despesa.");
            }
        }
        // LinkedHashSet elimina duplicatas mantendo ordem previsível
        return new LinkedHashSet<>(lista);
    }

    private int validarPrioridade(boolean essencial, int prioridade) {
        if (essencial) {
            return 0;
        }
        if (prioridade <= 0) {
            throw new IllegalArgumentException("Despesas opcionais devem ter prioridade inteira positiva (maior que 0).");
        }
        return prioridade;
    }

    /**
     * Calcula quanto cada participante selecionado pagará nesta despesa.
     */
    public abstract Map<Participante, BigDecimal> calcularRateio();

    /**
     * Retorna o custo total desta despesa no planejamento.
     */
    public abstract BigDecimal getCustoTotal();

    /**
     * Retorna o identificador textual do tipo da despesa para exibição na interface.
     */
    public abstract String getTipoDescricao();

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public Set<Participante> getParticipantes() {
        return Collections.unmodifiableSet(participantes);
    }

    public boolean isEssencial() {
        return essencial;
    }

    public int getPrioridade() {
        return prioridade;
    }
}