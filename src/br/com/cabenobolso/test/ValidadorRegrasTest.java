package br.com.cabenobolso.test;

import br.com.cabenobolso.controller.PlanejamentoController;
import br.com.cabenobolso.model.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class ValidadorRegrasTest {

    public static void main(String[] args) throws Exception {
        System.out.println("=== INICIANDO BATERIA COMPLETA DE TESTES (PASSOS 2 E 3) ===\n");

        testarCasoReferenciaSecao4();
        testarDistribuicaoCentavos();
        testarValidacoesEExcecoes();
        testarAlteracaoOrcamentoEExclusaoDespesa();
        testarPersistenciaEFalhaArquivo();
        testarSimuladorSecao7();

        System.out.println("\n=== TODOS OS 6 GRUPOS DE TESTES PASSARAM COM SUCESSO! ===");
    }

    private static void testarCasoReferenciaSecao4() {
        System.out.println("[Teste 1] Caso de Referência da Seção 4...");
        PlanejamentoController ctrl = new PlanejamentoController();
        ctrl.carregarCasoReferencia();

        Map<Participante, BigDecimal> totais = ctrl.calcularTotaisIndividuais();
        Map<Participante, BigDecimal> excedentes = ctrl.calcularExcedentes();

        Participante ana = ctrl.listarParticipantes().get(0);
        Participante bruno = ctrl.listarParticipantes().get(1);
        Participante carla = ctrl.listarParticipantes().get(2);

        verificar(new BigDecimal("75.00").equals(totais.get(ana)), "Total Ana esperado 75.00");
        verificar(new BigDecimal("60.00").equals(totais.get(bruno)), "Total Bruno esperado 60.00");
        verificar(new BigDecimal("75.00").equals(totais.get(carla)), "Total Carla esperado 75.00");
        verificar(new BigDecimal("210.00").equals(ctrl.calcularCustoTotalGeral()), "Custo geral esperado 210.00");

        verificar(new BigDecimal("5.00").equals(excedentes.get(ana)), "Excedente Ana esperado 5.00");
        verificar(new BigDecimal("0.00").equals(excedentes.get(bruno)), "Excedente Bruno esperado 0.00");
        verificar(new BigDecimal("5.00").equals(excedentes.get(carla)), "Excedente Carla esperado 5.00");
        System.out.println("  -> OK! Ana=75.00, Bruno=60.00, Carla=75.00, Geral=210.00.");
    }

    private static void testarDistribuicaoCentavos() {
        System.out.println("[Teste 2] Distribuição de centavos (R$ 100,00 / 3 participantes)...");
        PlanejamentoController ctrl = new PlanejamentoController();
        ctrl.cadastrarParticipante(3, "Carla", new BigDecimal("100.00"));
        ctrl.cadastrarParticipante(1, "Ana", new BigDecimal("100.00"));
        ctrl.cadastrarParticipante(2, "Bruno", new BigDecimal("100.00"));

        ctrl.cadastrarDespesa("FIXA", "Rateio 100", new BigDecimal("100.00"), Arrays.asList(3, 1, 2), true, 0);
        Map<Participante, BigDecimal> rateio = ctrl.listarDespesas().get(0).calcularRateio();

        Participante ana = ctrl.listarParticipantes().get(0);   // código 1
        Participante bruno = ctrl.listarParticipantes().get(1); // código 2
        Participante carla = ctrl.listarParticipantes().get(2); // código 3

        verificar(new BigDecimal("33.34").equals(rateio.get(ana)), "Código 1 deveria pagar 33.34");
        verificar(new BigDecimal("33.33").equals(rateio.get(bruno)), "Código 2 deveria pagar 33.33");
        verificar(new BigDecimal("33.33").equals(rateio.get(carla)), "Código 3 deveria pagar 33.33");
        System.out.println("  -> OK! 33.34, 33.33 e 33.33.");
    }

    private static void testarValidacoesEExcecoes() {
        System.out.println("[Teste 3] Bloqueio de entradas inválidas e integridade...");
        PlanejamentoController ctrl = new PlanejamentoController();
        ctrl.carregarCasoReferencia();

        esperarErro(() -> ctrl.cadastrarParticipante(1, "Duplicado", new BigDecimal("50.00")), "Código duplicado");
        esperarErro(() -> ctrl.cadastrarParticipante(9, "Negativo", new BigDecimal("-1.00")), "Orçamento negativo");
        esperarErro(() -> ctrl.cadastrarDespesa("FIXA", "Vazia", new BigDecimal("50.00"), Collections.emptyList(), true, 0), "Sem participantes");
        esperarErro(() -> ctrl.cadastrarDespesa("POR_PESSOA", "Zero", BigDecimal.ZERO, Arrays.asList(1), true, 0), "Valor zero");
        esperarErro(() -> ctrl.excluirParticipante(1), "Exclusão de participante vinculado");

        System.out.println("  -> OK! Regras defensivas validadas.");
    }

    private static void testarAlteracaoOrcamentoEExclusaoDespesa() {
        System.out.println("[Teste 4] Alteração de orçamento e exclusão de despesa...");
        PlanejamentoController ctrl = new PlanejamentoController();
        ctrl.carregarCasoReferencia();

        ctrl.editarParticipante(1, "Ana", new BigDecimal("80.00"));
        Participante ana = ctrl.listarParticipantes().get(0);
        verificar(new BigDecimal("0.00").equals(ctrl.calcularExcedentes().get(ana)), "Ana dentro do limite com 80.00");

        ctrl.excluirDespesa(2); // Remove Atividade recreativa
        for (BigDecimal total : ctrl.calcularTotaisIndividuais().values()) {
            verificar(new BigDecimal("60.00").equals(total), "Todos devem pagar 60.00");
        }
        verificar(new BigDecimal("180.00").equals(ctrl.calcularCustoTotalGeral()), "Custo geral esperado 180.00");
        System.out.println("  -> OK! Recálculos validados.");
    }

    private static void testarPersistenciaEFalhaArquivo() throws Exception {
        System.out.println("[Teste 5] Persistência em arquivo e proteção contra arquivo inválido...");
        String arquivoValido = "data/caso-referencia.dat";
        String arquivoCorrompido = "data/corrompido.tmp";

        PlanejamentoController ctrl1 = new PlanejamentoController();
        ctrl1.carregarCasoReferencia();
        ctrl1.salvarPlanejamento(arquivoValido);

        // Carregar em outro controller
        PlanejamentoController ctrl2 = new PlanejamentoController();
        ctrl2.carregarPlanejamento(arquivoValido);
        verificar(ctrl2.listarParticipantes().size() == 3, "Deveria recuperar 3 participantes");
        verificar(ctrl2.listarDespesas().size() == 4, "Deveria recuperar 4 despesas");
        verificar(new BigDecimal("210.00").equals(ctrl2.calcularCustoTotalGeral()), "Custo geral recuperado deveria ser 210.00");

        // Testar falha ao carregar arquivo inexistente (deve manter ctrl2 intacto!)
        try {
            ctrl2.carregarPlanejamento("data/nao_existe.dat");
            throw new AssertionError("Deveria lançar exceção para arquivo ausente");
        } catch (IOException esperado) {
            verificar(ctrl2.listarParticipantes().size() == 3, "Planejamento em uso foi perdido após falha!");
        }

        // Testar falha ao carregar arquivo corrompido
        try (FileWriter fw = new FileWriter(arquivoCorrompido)) {
            fw.write("conteudo invalido nao serializado");
        }
        try {
            ctrl2.carregarPlanejamento(arquivoCorrompido);
            throw new AssertionError("Deveria lançar exceção para arquivo corrompido");
        } catch (IOException esperado) {
            verificar(ctrl2.listarDespesas().size() == 4, "Planejamento em uso foi alterado após arquivo corrompido!");
        } finally {
            new File(arquivoCorrompido).delete();
        }

        System.out.println("  -> OK! Arquivo 'data/caso-referencia.dat' salvo, recuperado e falhas isoladas.");
    }

    private static void testarSimuladorSecao7() {
        System.out.println("[Teste 6] Simulador de alternativas (Bônus Seção 7)...");
        PlanejamentoController ctrl = new PlanejamentoController();
        ctrl.carregarCasoReferencia();

        // Caso viável: retira Sobremesa (prio 1), mantém Atividade (prio 3)
        OtimizadorDespesas.ResultadoOtimizacao resViavel = ctrl.executarSimulador();
        verificar(resViavel.isViavel(), "Deveria encontrar solução viável");
        verificar(resViavel.getSomaPrioridades() == 3, "Soma de prioridades esperada = 3");
        verificar(resViavel.getDespesasRetiradas().size() == 1, "Deveria retirar apenas 1 despesa (Sobremesa)");
        verificar("Sobremesa".equals(resViavel.getDespesasRetiradas().get(0).getDescricao()), "Despesa retirada deve ser Sobremesa");
        verificar(new BigDecimal("180.00").equals(resViavel.getNovoCustoTotalGeral()), "Novo custo geral esperado = 180.00");
        // Garante que o planejamento original NÃO foi modificado
        verificar(ctrl.listarDespesas().size() == 4, "Planejamento original não pode ser alterado pelo simulador");

        // Caso sem solução: orçamento da Ana = R$ 40,00 (essenciais custam R$ 50,00)
        ctrl.editarParticipante(1, "Ana", new BigDecimal("40.00"));
        OtimizadorDespesas.ResultadoOtimizacao resInviavel = ctrl.executarSimulador();
        verificar(!resInviavel.isViavel(), "Deveria indicar ausência de solução quando essenciais > orçamento");

        System.out.println("  -> OK! Simulador ótimo (R$ 180,00) e caso inviável validados.");
    }

    private static void verificar(boolean condicao, String mensagemErro) {
        if (!condicao) {
            throw new AssertionError("FALHA NA ASSERÇÃO: " + mensagemErro);
        }
    }

    private static void esperarErro(Runnable acao, String cenario) {
        try {
            acao.run();
            throw new AssertionError("FALHA: Era esperada exceção para '" + cenario + "'.");
        } catch (IllegalArgumentException | IllegalStateException esperado) {
            // Comportamento esperado
        }
    }
}