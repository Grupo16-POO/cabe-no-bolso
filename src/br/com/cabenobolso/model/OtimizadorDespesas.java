package br.com.cabenobolso.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OtimizadorDespesas {

    public static class ResultadoOtimizacao {
        private final boolean viavel;
        private final List<Despesa> despesasMantidas;
        private final List<Despesa> despesasRetiradas;
        private final int somaPrioridades;
        private final Map<Participante, BigDecimal> novosTotaisIndividuais;
        private final BigDecimal novoCustoTotalGeral;
        private final String mensagem;

        public ResultadoOtimizacao(boolean viavel, List<Despesa> mantidas, List<Despesa> retiradas,
                                   int somaPrioridades, Map<Participante, BigDecimal> novosTotais,
                                   BigDecimal novoCustoGeral, String mensagem) {
            this.viavel = viavel;
            this.despesasMantidas = Collections.unmodifiableList(mantidas);
            this.despesasRetiradas = Collections.unmodifiableList(retiradas);
            this.somaPrioridades = somaPrioridades;
            this.novosTotaisIndividuais = Collections.unmodifiableMap(novosTotais);
            this.novoCustoTotalGeral = novoCustoGeral;
            this.mensagem = mensagem;
        }

        public boolean isViavel() { return viavel; }
        public List<Despesa> getDespesasMantidas() { return despesasMantidas; }
        public List<Despesa> getDespesasRetiradas() { return despesasRetiradas; }
        public int getSomaPrioridades() { return somaPrioridades; }
        public Map<Participante, BigDecimal> getNovosTotaisIndividuais() { return novosTotaisIndividuais; }
        public BigDecimal getNovoCustoTotalGeral() { return novoCustoTotalGeral; }
        public String getMensagem() { return mensagem; }
    }

    public static ResultadoOtimizacao otimizar(Planejamento planejamento) {
        if (planejamento == null || planejamento.getParticipantesOrdenados().isEmpty()) {
            throw new IllegalArgumentException("Cadastre participantes e despesas antes de simular.");
        }

        List<Participante> participantes = planejamento.getParticipantesOrdenados();
        List<Despesa> essenciais = new ArrayList<>();
        List<Despesa> opcionais = new ArrayList<>();

        for (Despesa d : planejamento.getDespesas()) {
            if (d.isEssencial()) {
                essenciais.add(d);
            } else {
                opcionais.add(d);
            }
        }

        if (opcionais.size() > 10) {
            throw new IllegalArgumentException("O simulador suporta no máximo 10 despesas opcionais (atual: " + opcionais.size() + ").");
        }

        // 1. Verificar se apenas as despesas essenciais já excedem o orçamento de alguém
        Map<Participante, BigDecimal> gastosEssenciais = calcularGastos(participantes, essenciais);
        List<String> excessos = new ArrayList<>();
        for (Participante p : participantes) {
            BigDecimal gasto = gastosEssenciais.get(p);
            if (gasto.compareTo(p.getOrcamento()) > 0) {
                BigDecimal excesso = gasto.subtract(p.getOrcamento());
                excessos.add(p.getNome() + " (essenciais: R$ " + gasto + ", orçamento: R$ " + p.getOrcamento() + ", excesso: R$ " + excesso + ")");
            }
        }

        if (!excessos.isEmpty()) {
            String msg = "Sem solução viável: mesmo retirando todas as despesas opcionais, as despesas essenciais excedem o orçamento de: "
                    + String.join("; ", excessos) + ".";
            return new ResultadoOtimizacao(
                    false,
                    essenciais,
                    opcionais,
                    0,
                    gastosEssenciais,
                    calcularTotalGeral(essenciais),
                    msg
            );
        }

        // 2. Busca exaustiva (Power Set) nas 2^N combinações de despesas opcionais
        int n = opcionais.size();
        int totalCombinacoes = 1 << n;
        int melhorSomaPrioridades = -1;
        List<Despesa> melhoresOpcionais = new ArrayList<>();

        for (int mascara = 0; mascara < totalCombinacoes; mascara++) {
            List<Despesa> subconjuntoOpcional = new ArrayList<>();
            int somaPrioridadeAtual = 0;

            for (int bit = 0; bit < n; bit++) {
                if ((mascara & (1 << bit)) != 0) {
                    Despesa op = opcionais.get(bit);
                    subconjuntoOpcional.add(op);
                    somaPrioridadeAtual += op.getPrioridade();
                }
            }

            if (somaPrioridadeAtual > melhorSomaPrioridades) {
                List<Despesa> combinacaoCandidata = new ArrayList<>(essenciais);
                combinacaoCandidata.addAll(subconjuntoOpcional);

                if (cabeNoOrcamentoDeTodos(participantes, combinacaoCandidata)) {
                    melhorSomaPrioridades = somaPrioridadeAtual;
                    melhoresOpcionais = subconjuntoOpcional;
                }
            }
        }

        List<Despesa> mantidas = new ArrayList<>(essenciais);
        mantidas.addAll(melhoresOpcionais);

        List<Despesa> retiradas = new ArrayList<>();
        for (Despesa op : opcionais) {
            if (!melhoresOpcionais.contains(op)) {
                retiradas.add(op);
            }
        }

        Map<Participante, BigDecimal> totaisFinais = calcularGastos(participantes, mantidas);
        BigDecimal custoGeralFinal = calcularTotalGeral(mantidas);

        return new ResultadoOtimizacao(
                true,
                mantidas,
                retiradas,
                melhorSomaPrioridades,
                totaisFinais,
                custoGeralFinal,
                "Solução ótima encontrada mantendo todas as despesas essenciais."
        );
    }

    private static boolean cabeNoOrcamentoDeTodos(Collection<Participante> participantes, List<Despesa> despesas) {
        Map<Participante, BigDecimal> gastos = calcularGastos(participantes, despesas);
        for (Participante p : participantes) {
            if (gastos.get(p).compareTo(p.getOrcamento()) > 0) {
                return false;
            }
        }
        return true;
    }

    private static Map<Participante, BigDecimal> calcularGastos(Collection<Participante> participantes, List<Despesa> despesas) {
        Map<Participante, BigDecimal> gastos = new LinkedHashMap<>();
        for (Participante p : participantes) {
            gastos.put(p, BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY));
        }
        for (Despesa d : despesas) {
            for (Map.Entry<Participante, BigDecimal> item : d.calcularRateio().entrySet()) {
                Participante p = item.getKey();
                gastos.put(p, gastos.get(p).add(item.getValue()));
            }
        }
        return gastos;
    }

    private static BigDecimal calcularTotalGeral(List<Despesa> despesas) {
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
        for (Despesa d : despesas) {
            total = total.add(d.getCustoTotal());
        }
        return total;
    }
}