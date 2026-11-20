package br.com.cabenobolso.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DespesaFixa extends Despesa {
    private static final long serialVersionUID = 1L;

    public DespesaFixa(String descricao, BigDecimal valor, Collection<Participante> participantes) {
        super(descricao, valor, participantes, true, 0);
    }

    public DespesaFixa(String descricao, BigDecimal valor, Collection<Participante> participantes,
                       boolean essencial, int prioridade) {
        super(descricao, valor, participantes, essencial, prioridade);
    }

    @Override
    public Map<Participante, BigDecimal> calcularRateio() {
        List<Participante> lista = new ArrayList<>(getParticipantes());
        Collections.sort(lista);

        int qtd = lista.size();
        BigDecimal qtdBD = BigDecimal.valueOf(qtd);
        BigDecimal cotaBase = getValor().divide(qtdBD, 2, RoundingMode.DOWN);
        int centavosRestantes = getValor().subtract(cotaBase.multiply(qtdBD)).movePointRight(2).intValueExact();

        Map<Participante, BigDecimal> rateio = new LinkedHashMap<>();
        for (int i = 0; i < qtd; i++) {
            BigDecimal parcela = (i < centavosRestantes) ? cotaBase.add(new BigDecimal("0.01")) : cotaBase;
            rateio.put(lista.get(i), parcela);
        }
        return rateio;
    }

    @Override
    public BigDecimal getCustoTotal() { return getValor(); }

    @Override
    public String getTipoDescricao() { return "Fixa"; }
}