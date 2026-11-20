package br.com.cabenobolso.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Planejamento implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<Integer, Participante> participantes = new TreeMap<>();
    private final List<Despesa> despesas = new ArrayList<>();

    public void adicionarParticipante(Participante p) {
        if (p == null) throw new IllegalArgumentException("Participante inválido.");
        if (participantes.containsKey(p.getCodigo())) {
            throw new IllegalArgumentException("Já existe participante com o código " + p.getCodigo() + ".");
        }
        participantes.put(p.getCodigo(), p);
    }

    public void atualizarParticipante(int codigo, String novoNome, BigDecimal novoOrcamento) {
        buscarParticipantePorCodigo(codigo).atualizarDados(novoNome, novoOrcamento);
    }

    public void removerParticipante(int codigo) {
        Participante p = buscarParticipantePorCodigo(codigo);
        for (Despesa d : despesas) {
            if (d.getParticipantes().contains(p)) {
                throw new IllegalStateException("Não é possível excluir " + p.getNome() + " pois está na despesa '" + d.getDescricao() + "'.");
            }
        }
        participantes.remove(codigo);
    }

    public Participante buscarParticipantePorCodigo(int codigo) {
        Participante p = participantes.get(codigo);
        if (p == null) throw new IllegalArgumentException("Participante com código " + codigo + " não encontrado.");
        return p;
    }

    public List<Participante> getParticipantesOrdenados() { return new ArrayList<>(participantes.values()); }

    public void adicionarDespesa(Despesa d) {
        if (d == null) throw new IllegalArgumentException("Despesa inválida.");
        for (Participante p : d.getParticipantes()) {
            if (!participantes.containsKey(p.getCodigo())) {
                throw new IllegalArgumentException("Participante " + p.getNome() + " não pertence ao planejamento.");
            }
        }
        despesas.add(d);
    }

    public void substituirDespesa(int indice, Despesa nova) {
        if (indice < 0 || indice >= despesas.size()) throw new IndexOutOfBoundsException("Índice inválido.");
        if (nova == null) throw new IllegalArgumentException("Despesa inválida.");
        despesas.set(indice, nova);
    }

    public void removerDespesa(int indice) {
        if (indice < 0 || indice >= despesas.size()) throw new IndexOutOfBoundsException("Índice inválido.");
        despesas.remove(indice);
    }

    public void removerDespesa(Despesa d) { despesas.remove(d); }
    public List<Despesa> getDespesas() { return new ArrayList<>(despesas); }

    public Map<Participante, BigDecimal> calcularTotaisIndividuais() {
        Map<Participante, BigDecimal> totais = new LinkedHashMap<>();
        for (Participante p : participantes.values()) {
            totais.put(p, BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY));
        }
        for (Despesa d : despesas) {
            for (Map.Entry<Participante, BigDecimal> item : d.calcularRateio().entrySet()) {
                Participante p = item.getKey();
                totais.put(p, totais.get(p).add(item.getValue()));
            }
        }
        return totais;
    }

    public BigDecimal calcularCustoTotalGeral() {
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
        for (Despesa d : despesas) {
            total = total.add(d.getCustoTotal());
        }
        return total;
    }

    public Map<Participante, BigDecimal> calcularExcedentes() {
        Map<Participante, BigDecimal> totais = calcularTotaisIndividuais();
        Map<Participante, BigDecimal> excedentes = new LinkedHashMap<>();
        for (Participante p : participantes.values()) {
            BigDecimal diff = totais.get(p).subtract(p.getOrcamento());
            excedentes.put(p, diff.compareTo(BigDecimal.ZERO) > 0 ? diff : BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY));
        }
        return excedentes;
    }
}