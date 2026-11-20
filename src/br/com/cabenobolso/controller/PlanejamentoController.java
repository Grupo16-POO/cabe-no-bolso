package br.com.cabenobolso.controller;

import br.com.cabenobolso.model.*;
import br.com.cabenobolso.persistence.PersistenciaArquivo;
import br.com.cabenobolso.persistence.PersistenciaService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlanejamentoController {
    private Planejamento planejamento = new Planejamento();
    private final PersistenciaService persistenciaService = new PersistenciaArquivo();

    public void novoPlanejamento() { this.planejamento = new Planejamento(); }

    public void cadastrarParticipante(int codigo, String nome, BigDecimal orcamento) {
        planejamento.adicionarParticipante(new Participante(codigo, nome, orcamento));
    }

    public void editarParticipante(int codigo, String novoNome, BigDecimal novoOrcamento) {
        planejamento.atualizarParticipante(codigo, novoNome, novoOrcamento);
    }

    public void excluirParticipante(int codigo) { planejamento.removerParticipante(codigo); }
    public List<Participante> listarParticipantes() { return planejamento.getParticipantesOrdenados(); }

    public void cadastrarDespesa(String tipo, String descricao, BigDecimal valor,
                                 List<Integer> codigos, boolean essencial, int prioridade) {
        planejamento.adicionarDespesa(criarDespesa(tipo, descricao, valor, codigos, essencial, prioridade));
    }

    public void editarDespesa(int indice, String tipo, String descricao, BigDecimal valor,
                              List<Integer> codigos, boolean essencial, int prioridade) {
        planejamento.substituirDespesa(indice, criarDespesa(tipo, descricao, valor, codigos, essencial, prioridade));
    }

    public void excluirDespesa(int indice) { planejamento.removerDespesa(indice); }
    public List<Despesa> listarDespesas() { return planejamento.getDespesas(); }

    private Despesa criarDespesa(String tipo, String descricao, BigDecimal valor,
                                 List<Integer> codigos, boolean essencial, int prioridade) {
        if (codigos == null || codigos.isEmpty()) {
            throw new IllegalArgumentException("Selecione pelo menos um participante para a despesa.");
        }
        List<Participante> lista = new ArrayList<>();
        for (Integer cod : codigos) {
            lista.add(planejamento.buscarParticipantePorCodigo(cod));
        }
        if ("FIXA".equalsIgnoreCase(tipo)) {
            return new DespesaFixa(descricao, valor, lista, essencial, prioridade);
        } else if ("POR_PESSOA".equalsIgnoreCase(tipo)) {
            return new DespesaPorPessoa(descricao, valor, lista, essencial, prioridade);
        }
        throw new IllegalArgumentException("Tipo de despesa inválido: " + tipo);
    }

    public Map<Participante, BigDecimal> calcularTotaisIndividuais() { return planejamento.calcularTotaisIndividuais(); }
    public Map<Participante, BigDecimal> calcularExcedentes() { return planejamento.calcularExcedentes(); }
    public BigDecimal calcularCustoTotalGeral() { return planejamento.calcularCustoTotalGeral(); }
    public OtimizadorDespesas.ResultadoOtimizacao executarSimulador() { return OtimizadorDespesas.otimizar(planejamento); }

    public void salvarPlanejamento(String caminho) throws IOException {
        persistenciaService.salvar(planejamento, caminho);
    }

    public void carregarPlanejamento(String caminho) throws IOException, ClassNotFoundException {
        this.planejamento = persistenciaService.carregar(caminho);
    }

    public void carregarCasoReferencia() {
        Planejamento ref = new Planejamento();
        Participante ana = new Participante(1, "Ana", new BigDecimal("70.00"));
        Participante bruno = new Participante(2, "Bruno", new BigDecimal("60.00"));
        Participante carla = new Participante(3, "Carla", new BigDecimal("70.00"));

        ref.adicionarParticipante(ana);
        ref.adicionarParticipante(bruno);
        ref.adicionarParticipante(carla);

        ref.adicionarDespesa(new DespesaFixa("Transporte", new BigDecimal("90.00"), Arrays.asList(ana, bruno, carla), true, 0));
        ref.adicionarDespesa(new DespesaPorPessoa("Almoço", new BigDecimal("20.00"), Arrays.asList(ana, bruno, carla), true, 0));
        ref.adicionarDespesa(new DespesaFixa("Atividade recreativa", new BigDecimal("30.00"), Arrays.asList(ana, carla), false, 3));
        ref.adicionarDespesa(new DespesaPorPessoa("Sobremesa", new BigDecimal("10.00"), Arrays.asList(ana, bruno, carla), false, 1));

        this.planejamento = ref;
    }
}