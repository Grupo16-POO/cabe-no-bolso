package br.com.cabenobolso.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DespesaPorPessoa extends Despesa {
    private static final long serialVersionUID = 1L;

    public DespesaPorPessoa(String descricao, BigDecimal valorUnitario, Collection<Participante> participantes) {
        super(descricao, valorUnitario, participantes, true, 0);
    }

    public DespesaPorPessoa(String descricao, BigDecimal valorUnitario, Collection<Participante> participantes,
                            boolean essencial, int prioridade) {
        super(descricao, valorUnitario, participantes, essencial, prioridade);
    }

    @Override
    public Map<Participante, BigDecimal> calcularRateio() {
        List<Participante> lista = new ArrayList<>(getParticipantes());
        Collections.sort(lista);

        Map<Participante, BigDecimal> rateio = new LinkedHashMap<>();
        for (Participante p : lista) {
            rateio.put(p, getValor());
        }
        return rateio;
    }

    @Override
    public BigDecimal getCustoTotal() {
        BigDecimal qtd = BigDecimal.valueOf(getParticipantes().size());
        return getValor().multiply(qtd).setScale(2, RoundingMode.UNNECESSARY);
    }

    @Override
    public String getTipoDescricao() { return "Por pessoa"; }
}