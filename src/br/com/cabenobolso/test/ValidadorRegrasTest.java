package br.com.cabenobolso.test;

import br.com.cabenobolso.model.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class ValidadorRegrasTest {

    public static void main(String[] args) {
        System.out.println("=== INICIANDO BATERIA DE TESTES DO MODEL ===\n");

        testarCasoReferenciaSecao4();
        testarDistribuicaoCentavos();
        testarValidacoesEExcecoes();
        testarAlteracaoOrcamentoEExclusaoDespesa();

        System.out.println("\n=== TODOS OS TESTES DO PASSO 2 PASSARAM COM SUCESSO! ===");
    }

    private static void testarCasoReferenciaSecao4() {
        System.out.println("[Teste 1] Caso de Referência da Seção 4...");
        Planejamento plan = criarPlanejamentoReferencia();

        Map<Participante, BigDecimal> totais = plan.calcularTotaisIndividuais();
        Map<Participante, BigDecimal> excedentes = plan.calcularExcedentes();

        Participante ana = plan.buscarParticipantePorCodigo(1);
        Participante bruno = plan.buscarParticipantePorCodigo(2);
        Participante carla = plan.buscarParticipantePorCodigo(3);

        verificar(new BigDecimal("75.00").equals(totais.get(ana)), "Total Ana esperado 75.00, obtido " + totais.get(ana));
        verificar(new BigDecimal("60.00").equals(totais.get(bruno)), "Total Bruno esperado 60.00, obtido " + totais.get(bruno));
        verificar(new BigDecimal("75.00").equals(totais.get(carla)), "Total Carla esperado 75.00, obtido " + totais.get(carla));
        verificar(new BigDecimal("210.00").equals(plan.calcularCustoTotalGeral()), "Custo geral esperado 210.00");

        verificar(new BigDecimal("5.00").equals(excedentes.get(ana)), "Excedente Ana esperado 5.00");
        verificar(new BigDecimal("0.00").equals(excedentes.get(bruno)), "Excedente Bruno esperado 0.00");
        verificar(new BigDecimal("5.00").equals(excedentes.get(carla)), "Excedente Carla esperado 5.00");
        System.out.println("  -> OK! Ana=75.00 (excede 5.00), Bruno=60.00 (limite), Carla=75.00 (excede 5.00), Geral=210.00.");
    }

    private static void testarDistribuicaoCentavos() {
        System.out.println("[Teste 2] Distribuição de centavos (R$ 100,00 / 3 participantes)...");
        Planejamento plan = new Planejamento();
        Participante p3 = new Participante(3, "Carla", new BigDecimal("100.00"));
        Participante p1 = new Participante(1, "Ana", new BigDecimal("100.00"));
        Participante p2 = new Participante(2, "Bruno", new BigDecimal("100.00"));

        // Inseridos fora de ordem propositalmente para validar ordenação por código
        plan.adicionarParticipante(p3);
        plan.adicionarParticipante(p1);
        plan.adicionarParticipante(p2);

        Despesa d = new DespesaFixa("Rateio Teste", new BigDecimal("100.00"), Arrays.asList(p3, p1, p2));
        plan.adicionarDespesa(d);

        Map<Participante, BigDecimal> rateio = d.calcularRateio();
        verificar(new BigDecimal("33.34").equals(rateio.get(p1)), "Código 1 deveria pagar 33.34");
        verificar(new BigDecimal("33.33").equals(rateio.get(p2)), "Código 2 deveria pagar 33.33");
        verificar(new BigDecimal("33.33").equals(rateio.get(p3)), "Código 3 deveria pagar 33.33");
        System.out.println("  -> OK! Código 1=33.34, Código 2=33.33, Código 3=33.33.");
    }

    private static void testarValidacoesEExcecoes() {
        System.out.println("[Teste 3] Bloqueio de entradas inválidas e integridade...");
        Planejamento plan = criarPlanejamentoReferencia();

        // Código duplicado
        esperarErro(() -> plan.adicionarParticipante(new Participante(1, "Outro", new BigDecimal("50.00"))), "Código duplicado");
        // Orçamento negativo
        esperarErro(() -> new Participante(9, "Negativo", new BigDecimal("-10.00")), "Orçamento negativo");
        // Despesa sem participantes
        esperarErro(() -> new DespesaFixa("Vazia", new BigDecimal("50.00"), Collections.emptyList()), "Sem participantes");
        // Despesa valor zero ou negativo
        esperarErro(() -> new DespesaPorPessoa("Zero", BigDecimal.ZERO, plan.getParticipantesOrdenados()), "Valor zero");
        // Excluir participante vinculado a despesa
        esperarErro(() -> plan.removerParticipante(1), "Exclusão de participante vinculado");

        System.out.println("  -> OK! Todas as regras defensivas bloquearam estados inválidos.");
    }

    private static void testarAlteracaoOrcamentoEExclusaoDespesa() {
        System.out.println("[Teste 4] Alteração de orçamento e exclusão de despesa...");
        Planejamento plan = criarPlanejamentoReferencia();

        // Alterar orçamento da Ana para R$ 80,00 -> excedente vira 0,00
        plan.atualizarParticipante(1, "Ana", new BigDecimal("80.00"));
        Participante ana = plan.buscarParticipantePorCodigo(1);
        verificar(new BigDecimal("0.00").equals(plan.calcularExcedentes().get(ana)), "Ana deveria estar dentro do limite com 80.00");

        // Excluir Atividade recreativa (índice 2) -> todos ficam com R$ 60,00 e geral R$ 180,00
        plan.removerDespesa(2);
        Map<Participante, BigDecimal> totais = plan.calcularTotaisIndividuais();
        for (Participante p : plan.getParticipantesOrdenados()) {
            verificar(new BigDecimal("60.00").equals(totais.get(p)), p.getNome() + " deveria pagar 60.00");
        }
        verificar(new BigDecimal("180.00").equals(plan.calcularCustoTotalGeral()), "Custo geral deveria ser 180.00");
        System.out.println("  -> OK! Recálculos automáticos validados.");
    }

    private static Planejamento criarPlanejamentoReferencia() {
        Planejamento plan = new Planejamento();
        Participante ana = new Participante(1, "Ana", new BigDecimal("70.00"));
        Participante bruno = new Participante(2, "Bruno", new BigDecimal("60.00"));
        Participante carla = new Participante(3, "Carla", new BigDecimal("70.00"));

        plan.adicionarParticipante(ana);
        plan.adicionarParticipante(bruno);
        plan.adicionarParticipante(carla);

        plan.adicionarDespesa(new DespesaFixa("Transporte", new BigDecimal("90.00"), Arrays.asList(ana, bruno, carla)));
        plan.adicionarDespesa(new DespesaPorPessoa("Almoço", new BigDecimal("20.00"), Arrays.asList(ana, bruno, carla)));
        plan.adicionarDespesa(new DespesaFixa("Atividade recreativa", new BigDecimal("30.00"), Arrays.asList(ana, carla)));
        plan.adicionarDespesa(new DespesaPorPessoa("Sobremesa", new BigDecimal("10.00"), Arrays.asList(ana, bruno, carla)));
        return plan;
    }

    private static void verificar(boolean condicao, String mensagemErro) {
        if (!condicao) {
            throw new AssertionError("FALHA NA ASSERÇÃO: " + mensagemErro);
        }
    }

    private static void esperarErro(Runnable acao, String cenario) {
        try {
            acao.run();
            throw new AssertionError("FALHA: Era esperada uma exceção para o cenário '" + cenario + "'.");
        } catch (IllegalArgumentException | IllegalStateException esperado) {
            // Comportamento esperado
        }
    }
}