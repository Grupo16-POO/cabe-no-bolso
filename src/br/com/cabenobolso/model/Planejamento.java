package br.com.cabenobolso.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Planejamento implements Serializable {
    private static final long serialVersionUID = 1L;

    // TreeMap mantém os participantes automaticamente ordenados pelo código único
    private final Map<Integer, Participante> participantes;
    private final List<Despesa> despesas;

    public Planejamento() {
        this.participantes = new TreeMap<>();
        this.despesas = new ArrayList<>();
    }

    public void adicionarParticipante(Participante participante) {
        if (participante == null) {
            throw new IllegalArgumentException("Participante inválido.");
        }
        if (participantes.containsKey(participante.getCodigo())) {
            throw new IllegalArgumentException("Já existe um participante cadastrado com o código " + participante.getCodigo() + ".");
        }
        participantes.put(participante.getCodigo(), participante);
    }

    public void atualizarParticipante(int codigo, String novoNome, BigDecimal novoOrcamento) {
        Participante p = buscarParticipantePorCodigo(codigo);
        p.atualizarDados(novoNome, novoOrcamento);
    }

    public void removerParticipante(int codigo) {
        Participante p = buscarParticipantePorCodigo(codigo);
        for (Despesa d : despesas) {
            if (d.getParticipantes().contains(p)) {
                throw new IllegalStateException(
                    "Não é possível excluir " + p.getNome() + " porque está vinculado(a) à despesa '" + d.getDescricao() + "'."
                );
            }
        }
        participantes.remove(codigo);
    }

    public Participante buscarParticipantePorCodigo(int codigo) {
        Participante p = participantes.get(codigo);
        if (p == null) {
            throw new IllegalArgumentException("Participante com código " + codigo + " não encontrado.");
        }
        return p;
    }

    public List<Participante> getParticipantesOrdenados() {
        return Collections.unmodifiableList(new ArrayList<>(participantes.values()));
    }

    public void adicionarDespesa(Despesa despesa) {
        if (despesa == null) {
            throw new IllegalArgumentException("Despesa inválida.");
        }
        for (Participante p : despesa.getParticipantes()) {
            if (!participantes.containsKey(p.getCodigo())) {
                throw new IllegalArgumentException("Participante " + p.getNome() + " não pertence a este planejamento.");
            }
        }
        despesas.add(despesa);
    }

    public void substituirDespesa(int indice, Despesa novaDespesa) {
        if (indice < 0 || indice >= despesas.size()) {
            throw new IndexOutOfBoundsException("Índice de despesa inválido.");
        }
        if (novaDespesa == null) {
            throw new IllegalArgumentException("Despesa inválida.");
        }
        despesas.set(indice, novaDespesa);
    }

    public void removerDespesa(int indice) {
        if (indice < 0 || indice >= despesas.size()) {
            throw new IndexOutOfBoundsException("Índice de despesa inválido.");
        }
        despesas.remove(indice);
    }

    public void removerDespesa(Despesa despesa) {
        despesas.remove(despesa);
    }

    public List<Despesa> getDespesas() {
        return Collections.unmodifiableList(despesas);
    }

    /**
     * Retorna o total acumulado de cada participante cadastrado (zero para quem não participa de nenhuma despesa).
     */
    public Map<Participante, BigDecimal> calcularTotaisIndividuais() {
        Map<Participante, BigDecimal> totais = new LinkedHashMap<>();
        for (Participante p : participantes.values()) {
            totais.put(p, BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY));
        }

        for (Despesa d : despesas) {
            Map<Participante, BigDecimal> rateio = d.calcularRateio();
            for (Map.Entry<Participante, BigDecimal> entry : rateio.entrySet()) {
                Participante p = entry.getKey();
                BigDecimal acumulado = totais.get(p).add(entry.getValue());
                totais.put(p, acumulado);
            }
        }
        return totais;
    }

    /**
     * Retorna o custo total de todas as despesas cadastradas.
     */
    public BigDecimal calcularCustoTotalGeral() {
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
        for (Despesa d : despesas) {
            total = total.add(d.getCustoTotal());
        }
        return total;
    }

    /**
     * Retorna quanto cada participante excedeu do orçamento (R$ 0,00 se estiver dentro ou igual ao limite).
     */
    public Map<Participante, BigDecimal> calcularExcedentes() {
        Map<Participante, BigDecimal> totais = calcularTotaisIndividuais();
        Map<Participante, BigDecimal> excedentes = new LinkedHashMap<>();

        for (Participante p : participantes.values()) {
            BigDecimal gasto = totais.get(p);
            BigDecimal diferenca = gasto.subtract(p.getOrcamento());
            if (diferenca.compareTo(BigDecimal.ZERO) > 0) {
                excedentes.put(p, diferenca);
            } else {
                excedentes.put(p, BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY));
            }
        }
        return excedentes;
    }
}