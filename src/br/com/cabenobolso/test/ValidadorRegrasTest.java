package br.com.cabenobolso.test;

import br.com.cabenobolso.controller.PlanejamentoController;
import br.com.cabenobolso.model.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

public class ValidadorRegrasTest {

    public static void main(String[] args) throws Exception {
        testarCasoReferencia();
        testarCentavos();
        testarValidacoes();
        testarEdicaoEExclusao();
        testarPersistencia();
        testarSimulador();
        System.out.println("TODOS OS 6 TESTES PASSARAM COM SUCESSO!");
    }

    private static void testarCasoReferencia() {
        PlanejamentoController c = new PlanejamentoController();
        c.carregarCasoReferencia();
        Map<Participante, BigDecimal> totais = c.calcularTotaisIndividuais();
        List<Participante> ps = c.listarParticipantes();

        verificar(new BigDecimal("75.00").equals(totais.get(ps.get(0))), "Ana = 75.00");
        verificar(new BigDecimal("60.00").equals(totais.get(ps.get(1))), "Bruno = 60.00");
        verificar(new BigDecimal("75.00").equals(totais.get(ps.get(2))), "Carla = 75.00");
        verificar(new BigDecimal("210.00").equals(c.calcularCustoTotalGeral()), "Total = 210.00");
    }

    private static void testarCentavos() {
        PlanejamentoController c = new PlanejamentoController();
        c.cadastrarParticipante(3, "Carla", new BigDecimal("100.00"));
        c.cadastrarParticipante(1, "Ana", new BigDecimal("100.00"));
        c.cadastrarParticipante(2, "Bruno", new BigDecimal("100.00"));
        c.cadastrarDespesa("FIXA", "Rateio", new BigDecimal("100.00"), Arrays.asList(3, 1, 2), true, 0);

        Map<Participante, BigDecimal> rateio = c.listarDespesas().get(0).calcularRateio();
        List<Participante> ps = c.listarParticipantes();
        verificar(new BigDecimal("33.34").equals(rateio.get(ps.get(0))), "Cod 1 = 33.34");
        verificar(new BigDecimal("33.33").equals(rateio.get(ps.get(1))), "Cod 2 = 33.33");
        verificar(new BigDecimal("33.33").equals(rateio.get(ps.get(2))), "Cod 3 = 33.33");
    }

    private static void testarValidacoes() {
        PlanejamentoController c = new PlanejamentoController();
        c.carregarCasoReferencia();
        esperarErro(() -> c.cadastrarParticipante(1, "Dup", new BigDecimal("50.00")), "Código duplicado");
        esperarErro(() -> c.cadastrarParticipante(9, "Neg", new BigDecimal("-1.00")), "Orçamento negativo");
        esperarErro(() -> c.cadastrarDespesa("FIXA", "Vazia", new BigDecimal("50.00"), Collections.emptyList(), true, 0), "Sem participantes");
        esperarErro(() -> c.excluirParticipante(1), "Exclusão bloqueada");
    }

    private static void testarEdicaoEExclusao() {
        PlanejamentoController c = new PlanejamentoController();
        c.carregarCasoReferencia();
        c.editarParticipante(1, "Ana", new BigDecimal("80.00"));
        verificar(new BigDecimal("0.00").equals(c.calcularExcedentes().get(c.listarParticipantes().get(0))), "Ana sem excesso");

        c.excluirDespesa(2);
        verificar(new BigDecimal("180.00").equals(c.calcularCustoTotalGeral()), "Total após exclusão = 180.00");
    }

    private static void testarPersistencia() throws Exception {
        PlanejamentoController c1 = new PlanejamentoController();
        c1.carregarCasoReferencia();
        c1.salvarPlanejamento("data/caso-referencia.dat");

        PlanejamentoController c2 = new PlanejamentoController();
        c2.carregarPlanejamento("data/caso-referencia.dat");
        verificar(c2.listarParticipantes().size() == 3 && c2.listarDespesas().size() == 4, "Dados recuperados");

        try {
            c2.carregarPlanejamento("data/inexistente.dat");
            throw new AssertionError("Deveria falhar");
        } catch (IOException esperado) {
            verificar(c2.listarParticipantes().size() == 3, "Planejamento ativo preservado");
        }
    }

    private static void testarSimulador() {
        PlanejamentoController c = new PlanejamentoController();
        c.carregarCasoReferencia();

        OtimizadorDespesas.ResultadoOtimizacao res = c.executarSimulador();
        verificar(res.isViavel() && new BigDecimal("180.00").equals(res.getNovoCustoTotalGeral()), "Simulador viável");
        verificar(c.listarDespesas().size() == 4, "Original intacto");

        c.editarParticipante(1, "Ana", new BigDecimal("40.00"));
        verificar(!c.executarSimulador().isViavel(), "Simulador inviável");
    }

    private static void verificar(boolean ok, String msg) {
        if (!ok) throw new AssertionError("FALHA: " + msg);
    }

    private static void esperarErro(Runnable acao, String cenario) {
        try {
            acao.run();
            throw new AssertionError("Esperava exceção em: " + cenario);
        } catch (IllegalArgumentException | IllegalStateException ignored) { }
    }
}