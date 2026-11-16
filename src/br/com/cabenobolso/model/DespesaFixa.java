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
        List<Participante> selecionadosOrdenados = new ArrayList<>(getParticipantes());
        Collections.sort(selecionadosOrdenados); // Ordena pelo código crescente (Comparable)

        int quantidade = selecionadosOrdenados.size();
        BigDecimal qtdBD = BigDecimal.valueOf(quantidade);

        // Divisão inteira em centavos (arredondando para baixo)
        BigDecimal cotaBase = getValor().divide(qtdBD, 2, RoundingMode.DOWN);
        BigDecimal totalBase = cotaBase.multiply(qtdBD);
        BigDecimal sobra = getValor().subtract(totalBase);

        int centavosRestantes = sobra.movePointRight(2).intValueExact();

        Map<Participante, BigDecimal> rateio = new LinkedHashMap<>();
        for (int i = 0; i < quantidade; i++) {
            Participante p = selecionadosOrdenados.get(i);
            BigDecimal parcela = cotaBase;
            if (i < centavosRestantes) {
                parcela = parcela.add(new BigDecimal("0.01"));
            }
            rateio.put(p, parcela.setScale(2, RoundingMode.UNNECESSARY));
        }
        return rateio;
    }

    @Override
    public BigDecimal getCustoTotal() {
        return getValor();
    }

    @Override
    public String getTipoDescricao() {
        return "Fixa";
    }
}