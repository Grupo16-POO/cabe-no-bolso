package br.com.cabenobolso.controller;

import br.com.cabenobolso.model.Despesa;
import br.com.cabenobolso.model.DespesaFixa;
import br.com.cabenobolso.model.DespesaPorPessoa;
import br.com.cabenobolso.model.OtimizadorDespesas;
import br.com.cabenobolso.model.Participante;
import br.com.cabenobolso.model.Planejamento;
import br.com.cabenobolso.persistence.PersistenciaArquivo;
import br.com.cabenobolso.persistence.PersistenciaService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlanejamentoController {
    private Planejamento planejamento;
    private final PersistenciaService persistenciaService;

    public PlanejamentoController() {
        this.planejamento = new Planejamento();
        this.persistenciaService = new PersistenciaArquivo();
    }

    public void novoPlanejamento() {
        this.planejamento = new Planejamento();
    }

    // --- Operações de Participantes ---

    public void cadastrarParticipante(int codigo, String nome, BigDecimal orcamento) {
        Participante novo = new Participante(codigo, nome, orcamento);
        planejamento.adicionarParticipante(novo);
    }

    public void editarParticipante(int codigo, String novoNome, BigDecimal novoOrcamento) {
        planejamento.atualizarParticipante(codigo, novoNome, novoOrcamento);
    }

    public void excluirParticipante(int codigo) {
        planejamento.removerParticipante(codigo);
    }

    public List<Participante> listarParticipantes() {
        return planejamento.getParticipantesOrdenados();
    }

    // --- Operações de Despesas (Polimorfismo centralizado no Controller/Model) ---

    public void cadastrarDespesa(String tipo, String descricao, BigDecimal valor,
                                 List<Integer> codigosParticipantes, boolean essencial, int prioridade) {
        Despesa nova = construirDespesa(tipo, descricao, valor, codigosParticipantes, essencial, prioridade);
        planejamento.adicionarDespesa(nova);
    }

    public void editarDespesa(int indice, String tipo, String descricao, BigDecimal valor,
                              List<Integer> codigosParticipantes, boolean essencial, int prioridade) {
        Despesa atualizada = construirDespesa(tipo, descricao, valor, codigosParticipantes, essencial, prioridade);
        planejamento.substituirDespesa(indice, atualizada);
    }

    public void excluirDespesa(int indice) {
        planejamento.removerDespesa(indice);
    }

    public List<Despesa> listarDespesas() {
        return planejamento.getDespesas();
    }

    private Despesa construirDespesa(String tipo, String descricao, BigDecimal valor,
                                     List<Integer> codigosParticipantes, boolean essencial, int prioridade) {
        if (codigosParticipantes == null || codigosParticipantes.isEmpty()) {
            throw new IllegalArgumentException("Selecione pelo menos um participante para a despesa.");
        }

        List<Participante> selecionados = new ArrayList<>();
        for (Integer cod : codigosParticipantes) {
            selecionados.add(planejamento.buscarParticipantePorCodigo(cod));
        }

        if ("FIXA".equalsIgnoreCase(tipo)) {
            return new DespesaFixa(descricao, valor, selecionados, essencial, prioridade);
        } else if ("POR_PESSOA".equalsIgnoreCase(tipo)) {
            return new DespesaPorPessoa(descricao, valor, selecionados, essencial, prioridade);
        } else {
            throw new IllegalArgumentException("Tipo de despesa inválido: " + tipo);
        }
    }

    // --- Consultas e Cálculos ---

    public Map<Participante, BigDecimal> calcularTotaisIndividuais() {
        return planejamento.calcularTotaisIndividuais();
    }

    public Map<Participante, BigDecimal> calcularExcedentes() {
        return planejamento.calcularExcedentes();
    }

    public BigDecimal calcularCustoTotalGeral() {
        return planejamento.calcularCustoTotalGeral();
    }

    // --- Simulador (Objetivo Extra - Seção 7) ---

    public OtimizadorDespesas.ResultadoOtimizacao executarSimulador() {
        return OtimizadorDespesas.otimizar(planejamento);
    }

    // --- Persistência ---

    public void salvarPlanejamento(String caminhoArquivo) throws IOException {
        persistenciaService.salvar(planejamento, caminhoArquivo);
    }

    /**
     * Carrega um planejamento do arquivo. Se ocorrer qualquer falha, mantém o planejamento atual intacto.
     */
    public void carregarPlanejamento(String caminhoArquivo) throws IOException, ClassNotFoundException {
        Planejamento carregado = persistenciaService.carregar(caminhoArquivo);
        this.planejamento = carregado;
    }

    // --- Carga Rápida do Caso de Referência (Seção 4 e Seção 7) ---

    public void carregarCasoReferencia() {
        Planejamento ref = new Planejamento();
        Participante ana = new Participante(1, "Ana", new BigDecimal("70.00"));
        Participante bruno = new Participante(2, "Bruno", new BigDecimal("60.00"));
        Participante carla = new Participante(3, "Carla", new BigDecimal("70.00"));

        ref.adicionarParticipante(ana);
        ref.adicionarParticipante(bruno);
        ref.adicionarParticipante(carla);

        // Essenciais (Seção 4 + Seção 7)
        ref.adicionarDespesa(new DespesaFixa("Transporte", new BigDecimal("90.00"), Arrays.asList(ana, bruno, carla), true, 0));
        ref.adicionarDespesa(new DespesaPorPessoa("Almoço", new BigDecimal("20.00"), Arrays.asList(ana, bruno, carla), true, 0));
        // Opcionais com prioridades da Seção 7
        ref.adicionarDespesa(new DespesaFixa("Atividade recreativa", new BigDecimal("30.00"), Arrays.asList(ana, carla), false, 3));
        ref.adicionarDespesa(new DespesaPorPessoa("Sobremesa", new BigDecimal("10.00"), Arrays.asList(ana, bruno, carla), false, 1));

        this.planejamento = ref;
    }
}