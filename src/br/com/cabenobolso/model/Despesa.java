package br.com.cabenobolso.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
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

    public final void atualizarDados(String novaDescricao, BigDecimal novoValor,
                                     Collection<Participante> novosParticipantes,
                                     boolean essencial, int prioridade) {
        if (novaDescricao == null || novaDescricao.trim().isEmpty()) {
            throw new IllegalArgumentException("A descrição da despesa não pode ficar vazia.");
        }
        if (novoValor == null || novoValor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da despesa deve ser maior que R$ 0,00.");
        }
        if (novoValor.scale() > 2) {
            throw new IllegalArgumentException("O valor da despesa deve ter no máximo duas casas decimais.");
        }
        if (novosParticipantes == null || novosParticipantes.isEmpty()) {
            throw new IllegalArgumentException("Selecione pelo menos um participante para a despesa.");
        }
        for (Participante p : novosParticipantes) {
            if (p == null) {
                throw new IllegalArgumentException("Participante inválido na despesa.");
            }
        }
        if (!essencial && prioridade <= 0) {
            throw new IllegalArgumentException("Despesas opcionais devem ter prioridade maior que 0.");
        }

        this.descricao = novaDescricao.trim();
        this.valor = novoValor.setScale(2, RoundingMode.UNNECESSARY);
        this.participantes = new LinkedHashSet<>(novosParticipantes);
        this.essencial = essencial;
        this.prioridade = essencial ? 0 : prioridade;
    }

    public abstract Map<Participante, BigDecimal> calcularRateio();
    public abstract BigDecimal getCustoTotal();
    public abstract String getTipoDescricao();

    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
    public Set<Participante> getParticipantes() { return new LinkedHashSet<>(participantes); }
    public boolean isEssencial() { return essencial; }
    public int getPrioridade() { return prioridade; }
}