package br.com.cabenobolso.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OtimizadorDespesas {

    public static class ResultadoOtimizacao {
        private final boolean viavel;
        private final List<Despesa> despesasMantidas, despesasRetiradas;
        private final int somaPrioridades;
        private final Map<Participante, BigDecimal> novosTotaisIndividuais;
        private final BigDecimal novoCustoTotalGeral;
        private final String mensagem;

        public ResultadoOtimizacao(boolean viavel, List<Despesa> mantidas, List<Despesa> retiradas,
                                   int somaPrioridades, Map<Participante, BigDecimal> novosTotais,
                                   BigDecimal novoCustoGeral, String mensagem) {
            this.viavel = viavel;
            this.despesasMantidas = mantidas;
            this.despesasRetiradas = retiradas;
            this.somaPrioridades = somaPrioridades;
            this.novosTotaisIndividuais = novosTotais;
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
            if (d.isEssencial()) essenciais.add(d);
            else opcionais.add(d);
        }

        if (opcionais.size() > 10) {
            throw new IllegalArgumentException("O simulador suporta no máximo 10 despesas opcionais.");
        }

        Map<Participante, BigDecimal> gastosEssenciais = somarGastos(participantes, essenciais);
        List<String> excessos = new ArrayList<>();
        for (Participante p : participantes) {
            BigDecimal gasto = gastosEssenciais.get(p);
            if (gasto.compareTo(p.getOrcamento()) > 0) {
                excessos.add(p.getNome() + " (essenciais: R$ " + gasto + ", orçamento: R$ " + p.getOrcamento() + ")");
            }
        }

        if (!excessos.isEmpty()) {
            return new ResultadoOtimizacao(false, essenciais, opcionais, 0, gastosEssenciais, somarTotal(essenciais),
                    "Sem solução viável: despesas essenciais excedem o orçamento de " + String.join("; ", excessos) + ".");
        }

        int n = opcionais.size();
        int totalComb = 1 << n;
        int melhorPrio = -1;
        List<Despesa> melhoresOp = new ArrayList<>();

        for (int comb = 0; comb < totalComb; comb++) {
            List<Despesa> atuais = new ArrayList<>();
            int prioAtual = 0;
            for (int i = 0; i < n; i++) {
                if ((comb & (1 << i)) != 0) {
                    atuais.add(opcionais.get(i));
                    prioAtual += opcionais.get(i).getPrioridade();
                }
            }
            if (prioAtual > melhorPrio) {
                List<Despesa> candidata = new ArrayList<>(essenciais);
                candidata.addAll(atuais);
                if (cabeNoOrcamento(participantes, candidata)) {
                    melhorPrio = prioAtual;
                    melhoresOp = atuais;
                }
            }
        }

        List<Despesa> mantidas = new ArrayList<>(essenciais);
        mantidas.addAll(melhoresOp);
        List<Despesa> retiradas = new ArrayList<>();
        for (Despesa op : opcionais) {
            if (!melhoresOp.contains(op)) retiradas.add(op);
        }

        return new ResultadoOtimizacao(true, mantidas, retiradas, melhorPrio,
                somarGastos(participantes, mantidas), somarTotal(mantidas),
                "Solução ótima encontrada mantendo todas as despesas essenciais.");
    }

    private static boolean cabeNoOrcamento(List<Participante> participantes, List<Despesa> lista) {
        Map<Participante, BigDecimal> gastos = somarGastos(participantes, lista);
        for (Participante p : participantes) {
            if (gastos.get(p).compareTo(p.getOrcamento()) > 0) return false;
        }
        return true;
    }

    private static Map<Participante, BigDecimal> somarGastos(List<Participante> participantes, List<Despesa> lista) {
        Map<Participante, BigDecimal> gastos = new LinkedHashMap<>();
        for (Participante p : participantes) gastos.put(p, BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY));
        for (Despesa d : lista) {
            for (Map.Entry<Participante, BigDecimal> item : d.calcularRateio().entrySet()) {
                Participante p = item.getKey();
                gastos.put(p, gastos.get(p).add(item.getValue()));
            }
        }
        return gastos;
    }

    private static BigDecimal somarTotal(List<Despesa> lista) {
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
        for (Despesa d : lista) total = total.add(d.getCustoTotal());
        return total;
    }
}