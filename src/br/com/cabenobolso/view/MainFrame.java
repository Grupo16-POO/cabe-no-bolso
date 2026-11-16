package br.com.cabenobolso.view;

import br.com.cabenobolso.controller.PlanejamentoController;
import br.com.cabenobolso.model.Despesa;
import br.com.cabenobolso.model.OtimizadorDespesas;
import br.com.cabenobolso.model.Participante;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainFrame extends JFrame {
    private static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");
    private static final NumberFormat MOEDA_FMT = NumberFormat.getCurrencyInstance(LOCALE_BR);

    private final PlanejamentoController controller;

    // --- Componentes Aba 1: Participantes ---
    private JTextField txtPartCodigo;
    private JTextField txtPartNome;
    private JTextField txtPartOrcamento;
    private DefaultTableModel modelParticipantes;
    private JTable tblParticipantes;

    // --- Componentes Aba 2: Despesas ---
    private JTextField txtDespDescricao;
    private JTextField txtDespValor;
    private JComboBox<String> cbDespTipo;
    private JCheckBox chkDespEssencial;
    private JSpinner spnDespPrioridade;
    private JPanel pnlCheckboxesParticipantes;
    private final Map<Integer, JCheckBox> mapaCheckboxes = new LinkedHashMap<>();
    private DefaultTableModel modelDespesas;
    private JTable tblDespesas;

    // --- Componentes Aba 3: Resumo ---
    private JLabel lblCustoTotalGeral;
    private JLabel lblQtdParticipantes;
    private JLabel lblQtdDespesas;
    private JLabel lblStatusGeral;
    private DefaultTableModel modelResumoIndividual;
    private JTable tblResumoIndividual;
    private DefaultTableModel modelMatrizRateio;
    private JTable tblMatrizRateio;

    // --- Componentes Aba 4: Simulador (Bônus) ---
    private JLabel lblStatusSimulador;
    private JTextArea txtDetalhesSimulador;
    private DefaultTableModel modelComparativoSimulador;
    private JTable tblComparativoSimulador;

    public MainFrame() {
        this.controller = new PlanejamentoController();
        configurarJanela();
        inicializarComponentes();
        atualizarTodaInterface();
    }

    private void configurarJanela() {
        setTitle("Cabe no Bolso - Planejador de Despesas Compartilhadas");
        setSize(1020, 700);
        setMinimumSize(new Dimension(900, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
    }

    private void inicializarComponentes() {
        add(criarBarraSuperior(), BorderLayout.NORTH);

        JTabbedPane abas = new JTabbedPane();
        abas.setFont(new Font("SansSerif", Font.BOLD, 13));
        abas.addTab("1. Participantes", criarAbaParticipantes());
        abas.addTab("2. Despesas", criarAbaDespesas());
        abas.addTab("3. Resumo do Planejamento", criarAbaResumo());
        abas.addTab("4. Simulador de Alternativas (Bônus)", criarAbaSimulador());

        add(abas, BorderLayout.CENTER);
    }

    // =========================================================================
    // BARRA SUPERIOR (ARQUIVO E CASO DE REFERÊNCIA)
    // =========================================================================
    private JPanel criarBarraSuperior() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JButton btnNovo = new JButton("Novo Planejamento (Vazio)");
        JButton btnSalvar = new JButton("Salvar Planejamento (.dat)");
        JButton btnCarregar = new JButton("Carregar Planejamento (.dat)");
        JButton btnReferencia = new JButton("Carregar Caso de Referência (Seção 4)");

        btnReferencia.setFont(new Font("SansSerif", Font.BOLD, 12));

        btnNovo.addActionListener(e -> acaoNovoPlanejamento());
        btnSalvar.addActionListener(e -> acaoSalvarArquivo());
        btnCarregar.addActionListener(e -> acaoCarregarArquivo());
        btnReferencia.addActionListener(e -> acaoCarregarCasoReferencia());

        barra.add(btnNovo);
        barra.add(btnSalvar);
        barra.add(btnCarregar);
        barra.add(new JSeparator(SwingConstants.VERTICAL));
        barra.add(btnReferencia);

        return barra;
    }

    // =========================================================================
    // ABA 1: PARTICIPANTES
    // =========================================================================
    private JPanel criarAbaParticipantes() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Formulário superior
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(new TitledBorder("Cadastro e Edição de Participante"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtPartCodigo = new JTextField(8);
        txtPartNome = new JTextField(22);
        txtPartOrcamento = new JTextField(12);

        gbc.gridx = 0; gbc.gridy = 0;
        pnlForm.add(new JLabel("Código Único:"), gbc);
        gbc.gridx = 1;
        pnlForm.add(txtPartCodigo, gbc);

        gbc.gridx = 2;
        pnlForm.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0;
        pnlForm.add(txtPartNome, gbc);

        gbc.gridx = 4; gbc.weightx = 0;
        pnlForm.add(new JLabel("Orçamento (R$):"), gbc);
        gbc.gridx = 5;
        pnlForm.add(txtPartOrcamento, gbc);

        // Botões de ação
        JPanel pnlBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnCadastrar = new JButton("Cadastrar");
        JButton btnEditar = new JButton("Salvar Alterações do Selecionado");
        JButton btnExcluir = new JButton("Excluir Selecionado");
        JButton btnLimpar = new JButton("Limpar Campos");

        btnCadastrar.addActionListener(e -> acaoCadastrarParticipante());
        btnEditar.addActionListener(e -> acaoEditarParticipante());
        btnExcluir.addActionListener(e -> acaoExcluirParticipante());
        btnLimpar.addActionListener(e -> limparCamposParticipante());

        pnlBotoes.add(btnCadastrar);
        pnlBotoes.add(btnEditar);
        pnlBotoes.add(btnExcluir);
        pnlBotoes.add(btnLimpar);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 6;
        pnlForm.add(pnlBotoes, gbc);

        // Tabela de participantes
        modelParticipantes = new DefaultTableModel(new String[]{"Código", "Nome", "Orçamento Disponível"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblParticipantes = new JTable(modelParticipantes);
        tblParticipantes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblParticipantes.setRowHeight(24);

        tblParticipantes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblParticipantes.getSelectedRow() >= 0) {
                int row = tblParticipantes.getSelectedRow();
                int cod = (int) modelParticipantes.getValueAt(row, 0);
                Participante p = buscarParticipanteLocal(cod);
                if (p != null) {
                    txtPartCodigo.setText(String.valueOf(p.getCodigo()));
                    txtPartCodigo.setEditable(false);
                    txtPartNome.setText(p.getNome());
                    txtPartOrcamento.setText(p.getOrcamento().toPlainString().replace(".", ","));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblParticipantes);
        scroll.setBorder(new TitledBorder("Participantes Cadastrados (clique em uma linha para editar ou excluir)"));

        painel.add(pnlForm, BorderLayout.NORTH);
        painel.add(scroll, BorderLayout.CENTER);
        return painel;
    }

    // =========================================================================
    // ABA 2: DESPESAS
    // =========================================================================
    private JPanel criarAbaDespesas() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel pnlSuperior = new JPanel(new BorderLayout(10, 10));
        pnlSuperior.setBorder(new TitledBorder("Cadastro e Edição de Despesa"));

        // Campos da esquerda
        JPanel pnlCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtDespDescricao = new JTextField(20);
        txtDespValor = new JTextField(10);
        cbDespTipo = new JComboBox<>(new String[]{"Fixa (divide entre selecionados)", "Por pessoa (valor unitário)"});
        chkDespEssencial = new JCheckBox("Despesa Essencial (obrigatória no simulador)", true);
        spnDespPrioridade = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        spnDespPrioridade.setEnabled(false);

        chkDespEssencial.addActionListener(e -> spnDespPrioridade.setEnabled(!chkDespEssencial.isSelected()));

        gbc.gridx = 0; gbc.gridy = 0;
        pnlCampos.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        pnlCampos.add(txtDespDescricao, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        pnlCampos.add(new JLabel("Valor (R$):"), gbc);
        gbc.gridx = 1;
        pnlCampos.add(txtDespValor, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        pnlCampos.add(new JLabel("Tipo de Rateio:"), gbc);
        gbc.gridx = 1;
        pnlCampos.add(cbDespTipo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        pnlCampos.add(chkDespEssencial, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        pnlCampos.add(new JLabel("Prioridade (se opcional):"), gbc);
        gbc.gridx = 1;
        pnlCampos.add(spnDespPrioridade, gbc);

        // Painel da direita: Seleção de participantes envolvidos
        JPanel pnlDireita = new JPanel(new BorderLayout(4, 4));
        pnlDireita.setBorder(new TitledBorder("Participantes Envolvidos na Despesa"));
        pnlDireita.setPreferredSize(new Dimension(340, 170));

        pnlCheckboxesParticipantes = new JPanel();
        pnlCheckboxesParticipantes.setLayout(new BoxLayout(pnlCheckboxesParticipantes, BoxLayout.Y_AXIS));
        JScrollPane scrollChecks = new JScrollPane(pnlCheckboxesParticipantes);

        JPanel pnlAcoesChecks = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        JButton btnMarcarTodos = new JButton("Marcar Todos");
        JButton btnDesmarcarTodos = new JButton("Desmarcar Todos");
        btnMarcarTodos.addActionListener(e -> mapaCheckboxes.values().forEach(cb -> cb.setSelected(true)));
        btnDesmarcarTodos.addActionListener(e -> mapaCheckboxes.values().forEach(cb -> cb.setSelected(false)));
        pnlAcoesChecks.add(btnMarcarTodos);
        pnlAcoesChecks.add(btnDesmarcarTodos);

        pnlDireita.add(scrollChecks, BorderLayout.CENTER);
        pnlDireita.add(pnlAcoesChecks, BorderLayout.SOUTH);

        // Botões inferiores do formulário
        JPanel pnlBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnAdicionar = new JButton("Adicionar Despesa");
        JButton btnEditar = new JButton("Salvar Alterações da Selecionada");
        JButton btnExcluir = new JButton("Excluir Selecionada");
        JButton btnLimpar = new JButton("Limpar Campos");

        btnAdicionar.addActionListener(e -> acaoAdicionarDespesa());
        btnEditar.addActionListener(e -> acaoEditarDespesa());
        btnExcluir.addActionListener(e -> acaoExcluirDespesa());
        btnLimpar.addActionListener(e -> limparCamposDespesa());

        pnlBotoes.add(btnAdicionar);
        pnlBotoes.add(btnEditar);
        pnlBotoes.add(btnExcluir);
        pnlBotoes.add(btnLimpar);

        pnlSuperior.add(pnlCampos, BorderLayout.CENTER);
        pnlSuperior.add(pnlDireita, BorderLayout.EAST);
        pnlSuperior.add(pnlBotoes, BorderLayout.SOUTH);

        // Tabela de despesas
        modelDespesas = new DefaultTableModel(
                new String[]{"#", "Descrição", "Tipo", "Valor Informado", "Custo Total", "Classificação", "Participantes"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblDespesas = new JTable(modelDespesas);
        tblDespesas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDespesas.setRowHeight(24);

        tblDespesas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblDespesas.getSelectedRow() >= 0) {
                carregarDespesaNoFormulario(tblDespesas.getSelectedRow());
            }
        });

        JScrollPane scrollTabela = new JScrollPane(tblDespesas);
        scrollTabela.setBorder(new TitledBorder("Despesas Lançadas (clique em uma linha para editar ou excluir)"));

        painel.add(pnlSuperior, BorderLayout.NORTH);
        painel.add(scrollTabela, BorderLayout.CENTER);
        return painel;
    }

    // =========================================================================
    // ABA 3: RESUMO DO PLANEJAMENTO
    // =========================================================================
    private JPanel criarAbaResumo() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Indicadores superiores
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 10, 10));
        lblCustoTotalGeral = criarLabelIndicador("Custo Total: R$ 0,00");
        lblQtdParticipantes = criarLabelIndicador("Participantes: 0");
        lblQtdDespesas = criarLabelIndicador("Despesas: 0");
        lblStatusGeral = criarLabelIndicador("Situação: Sem dados");

        pnlCards.add(lblCustoTotalGeral);
        pnlCards.add(lblQtdParticipantes);
        pnlCards.add(lblQtdDespesas);
        pnlCards.add(lblStatusGeral);

        // Tabela 1: Situação Individual em relação ao orçamento
        modelResumoIndividual = new DefaultTableModel(
                new String[]{"Código", "Participante", "Orçamento", "Total Individual", "Valor Excedido", "Situação"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblResumoIndividual = new JTable(modelResumoIndividual);
        tblResumoIndividual.setRowHeight(24);
        JScrollPane scrollIndividual = new JScrollPane(tblResumoIndividual);
        scrollIndividual.setBorder(new TitledBorder("Situação Individual vs. Orçamento"));

        // Tabela 2: Matriz detalhada (Pessoa x Despesa) igual à Seção 4
        modelMatrizRateio = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblMatrizRateio = new JTable(modelMatrizRateio);
        tblMatrizRateio.setRowHeight(24);
        JScrollPane scrollMatriz = new JScrollPane(tblMatrizRateio);
        scrollMatriz.setBorder(new TitledBorder("Detalhamento do Rateio por Despesa (Valores em R$)"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollIndividual, scrollMatriz);
        split.setResizeWeight(0.5);

        painel.add(pnlCards, BorderLayout.NORTH);
        painel.add(split, BorderLayout.CENTER);
        return painel;
    }

    // =========================================================================
    // ABA 4: SIMULADOR DE ALTERNATIVAS (BÔNUS - SEÇÃO 7)
    // =========================================================================
    private JPanel criarAbaSimulador() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel pnlTopo = new JPanel(new BorderLayout(10, 10));
        JButton btnSimular = new JButton("Executar Simulação de Alternativas (Manter Essenciais + Maximizar Prioridade)");
        btnSimular.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnSimular.addActionListener(e -> acaoExecutarSimulador());

        lblStatusSimulador = new JLabel("Clique no botão acima para simular ajustes sem alterar o planejamento original.");
        lblStatusSimulador.setFont(new Font("SansSerif", Font.BOLD, 13));

        pnlTopo.add(btnSimular, BorderLayout.NORTH);
        pnlTopo.add(lblStatusSimulador, BorderLayout.SOUTH);

        txtDetalhesSimulador = new JTextArea(9, 40);
        txtDetalhesSimulador.setEditable(false);
        txtDetalhesSimulador.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scrollDetalhes = new JScrollPane(txtDetalhesSimulador);
        scrollDetalhes.setBorder(new TitledBorder("Despesas Mantidas e Retiradas na Simulação"));

        modelComparativoSimulador = new DefaultTableModel(
                new String[]{"Código", "Participante", "Orçamento", "Gasto Original", "Gasto Simulado", "Nova Situação"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblComparativoSimulador = new JTable(modelComparativoSimulador);
        tblComparativoSimulador.setRowHeight(24);
        JScrollPane scrollComparativo = new JScrollPane(tblComparativoSimulador);
        scrollComparativo.setBorder(new TitledBorder("Comparativo Individual (Antes x Depois da Simulação)"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollDetalhes, scrollComparativo);
        split.setResizeWeight(0.45);

        painel.add(pnlTopo, BorderLayout.NORTH);
        painel.add(split, BorderLayout.CENTER);
        return painel;
    }

    // =========================================================================
    // AÇÕES DOS BOTÕES E EVENTOS
    // =========================================================================
    private void acaoNovoPlanejamento() {
        int resp = JOptionPane.showConfirmDialog(this,
                "Deseja iniciar um planejamento vazio? Os dados não salvos serão perdidos.",
                "Novo Planejamento", JOptionPane.YES_NO_OPTION);
        if (resp == JOptionPane.YES_OPTION) {
            controller.novoPlanejamento();
            limparCamposParticipante();
            limparCamposDespesa();
            limparTelaSimulador();
            atualizarTodaInterface();
        }
    }

    private void acaoSalvarArquivo() {
        JFileChooser chooser = new JFileChooser(new File("data"));
        chooser.setDialogTitle("Salvar Planejamento");
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Planejamento (*.dat)", "dat"));
        chooser.setSelectedFile(new File("meu-planejamento.dat"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = chooser.getSelectedFile();
            String caminho = arquivo.getAbsolutePath();
            if (!caminho.toLowerCase().endsWith(".dat")) {
                caminho += ".dat";
            }
            try {
                controller.salvarPlanejamento(caminho);
                JOptionPane.showMessageDialog(this, "Planejamento salvo com sucesso em:\n" + caminho,
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                mostrarErro("Falha ao salvar arquivo: " + ex.getMessage());
            }
        }
    }

    private void acaoCarregarArquivo() {
        JFileChooser chooser = new JFileChooser(new File("data"));
        chooser.setDialogTitle("Carregar Planejamento");
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Planejamento (*.dat)", "dat"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                controller.carregarPlanejamento(chooser.getSelectedFile().getAbsolutePath());
                limparCamposParticipante();
                limparCamposDespesa();
                limparTelaSimulador();
                atualizarTodaInterface();
                JOptionPane.showMessageDialog(this, "Planejamento carregado com sucesso!",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                mostrarErro("Não foi possível carregar o arquivo selecionado.\nO planejamento atual foi preservado.\n\nDetalhe: " + ex.getMessage());
            }
        }
    }

    private void acaoCarregarCasoReferencia() {
        controller.carregarCasoReferencia();
        limparCamposParticipante();
        limparCamposDespesa();
        limparTelaSimulador();
        atualizarTodaInterface();
        JOptionPane.showMessageDialog(this,
                "Caso de referência da Seção 4 carregado (Ana, Bruno e Carla - Total R$ 210,00).",
                "Caso de Referência", JOptionPane.INFORMATION_MESSAGE);
    }

    private void acaoCadastrarParticipante() {
        try {
            int codigo = Integer.parseInt(txtPartCodigo.getText().trim());
            String nome = txtPartNome.getText();
            BigDecimal orcamento = parseValorMonetario(txtPartOrcamento.getText());

            controller.cadastrarParticipante(codigo, nome, orcamento);
            limparCamposParticipante();
            atualizarTodaInterface();
        } catch (NumberFormatException ex) {
            mostrarErro("Informe um código inteiro válido e um orçamento numérico (ex: 70,00).");
        } catch (Exception ex) {
            mostrarErro(ex.getMessage());
        }
    }

    private void acaoEditarParticipante() {
        int row = tblParticipantes.getSelectedRow();
        if (row < 0) {
            mostrarErro("Selecione um participante na tabela antes de salvar alterações.");
            return;
        }
        try {
            int codigo = (int) modelParticipantes.getValueAt(row, 0);
            String novoNome = txtPartNome.getText();
            BigDecimal novoOrcamento = parseValorMonetario(txtPartOrcamento.getText());

            controller.editarParticipante(codigo, novoNome, novoOrcamento);
            limparCamposParticipante();
            atualizarTodaInterface();
        } catch (Exception ex) {
            mostrarErro(ex.getMessage());
        }
    }

    private void acaoExcluirParticipante() {
        int row = tblParticipantes.getSelectedRow();
        if (row < 0) {
            mostrarErro("Selecione um participante na tabela para excluir.");
            return;
        }
        try {
            int codigo = (int) modelParticipantes.getValueAt(row, 0);
            controller.excluirParticipante(codigo);
            limparCamposParticipante();
            atualizarTodaInterface();
        } catch (Exception ex) {
            mostrarErro(ex.getMessage());
        }
    }

    private void acaoAdicionarDespesa() {
        try {
            String tipo = cbDespTipo.getSelectedIndex() == 0 ? "FIXA" : "POR_PESSOA";
            String descricao = txtDespDescricao.getText();
            BigDecimal valor = parseValorMonetario(txtDespValor.getText());
            boolean essencial = chkDespEssencial.isSelected();
            int prioridade = essencial ? 0 : (Integer) spnDespPrioridade.getValue();
            List<Integer> selecionados = obterCodigosSelecionados();

            controller.cadastrarDespesa(tipo, descricao, valor, selecionados, essencial, prioridade);
            limparCamposDespesa();
            atualizarTodaInterface();
        } catch (Exception ex) {
            mostrarErro(ex.getMessage());
        }
    }

    private void acaoEditarDespesa() {
        int row = tblDespesas.getSelectedRow();
        if (row < 0) {
            mostrarErro("Selecione uma despesa na tabela antes de salvar alterações.");
            return;
        }
        try {
            String tipo = cbDespTipo.getSelectedIndex() == 0 ? "FIXA" : "POR_PESSOA";
            String descricao = txtDespDescricao.getText();
            BigDecimal valor = parseValorMonetario(txtDespValor.getText());
            boolean essencial = chkDespEssencial.isSelected();
            int prioridade = essencial ? 0 : (Integer) spnDespPrioridade.getValue();
            List<Integer> selecionados = obterCodigosSelecionados();

            controller.editarDespesa(row, tipo, descricao, valor, selecionados, essencial, prioridade);
            limparCamposDespesa();
            atualizarTodaInterface();
        } catch (Exception ex) {
            mostrarErro(ex.getMessage());
        }
    }

    private void acaoExcluirDespesa() {
        int row = tblDespesas.getSelectedRow();
        if (row < 0) {
            mostrarErro("Selecione uma despesa na tabela para excluir.");
            return;
        }
        controller.excluirDespesa(row);
        limparCamposDespesa();
        atualizarTodaInterface();
    }

    private void acaoExecutarSimulador() {
        try {
            OtimizadorDespesas.ResultadoOtimizacao res = controller.executarSimulador();
            Map<Participante, BigDecimal> totaisOriginais = controller.calcularTotaisIndividuais();

            modelComparativoSimulador.setRowCount(0);
            StringBuilder sb = new StringBuilder();

            if (!res.isViavel()) {
                lblStatusSimulador.setText("RESULTADO: SEM SOLUÇÃO VIÁVEL (Essenciais excedem o orçamento)");
                lblStatusSimulador.setForeground(new Color(180, 0, 0));

                sb.append("MOTIVO DA INVIABILIDADE:\n");
                sb.append(res.getMensagem()).append("\n\n");
                sb.append("Nota: O planejamento original permaneceu inalterado.\n");
            } else {
                lblStatusSimulador.setText("RESULTADO: SOLUÇÃO VIÁVEL ENCONTRADA! Novo Custo Geral: "
                        + formatarMoeda(res.getNovoCustoTotalGeral()) + " (Prioridade acumulada: " + res.getSomaPrioridades() + ")");
                lblStatusSimulador.setForeground(new Color(0, 120, 0));

                sb.append("=== DESPESAS MANTIDAS ===\n");
                for (Despesa d : res.getDespesasMantidas()) {
                    String tag = d.isEssencial() ? "[Essencial]" : "[Opcional - Prio " + d.getPrioridade() + "]";
                    sb.append(String.format(" • %-25s | %-20s | Custo: %s%n",
                            d.getDescricao(), tag, formatarMoeda(d.getCustoTotal())));
                }

                sb.append("\n=== DESPESAS RETIRADAS PARA CABER NO ORÇAMENTO ===\n");
                if (res.getDespesasRetiradas().isEmpty()) {
                    sb.append(" • Nenhuma despesa precisou ser retirada! Todas cabem no orçamento.\n");
                } else {
                    for (Despesa d : res.getDespesasRetiradas()) {
                        sb.append(String.format(" • %-25s | [Opcional - Prio %d] | Economia: %s%n",
                                d.getDescricao(), d.getPrioridade(), formatarMoeda(d.getCustoTotal())));
                    }
                }
            }

            for (Participante p : controller.listarParticipantes()) {
                BigDecimal orig = totaisOriginais.get(p);
                BigDecimal sim = res.getNovosTotaisIndividuais().get(p);
                String sit = sim.compareTo(p.getOrcamento()) <= 0
                        ? "Dentro do orçamento"
                        : "Excede " + formatarMoeda(sim.subtract(p.getOrcamento()));

                modelComparativoSimulador.addRow(new Object[]{
                        p.getCodigo(), p.getNome(), formatarMoeda(p.getOrcamento()),
                        formatarMoeda(orig), formatarMoeda(sim), sit
                });
            }

            txtDetalhesSimulador.setText(sb.toString());
        } catch (Exception ex) {
            mostrarErro(ex.getMessage());
        }
    }

    // =========================================================================
    // ATUALIZAÇÃO SINCRONIZADA DE TODAS AS TELAS
    // =========================================================================
    private void atualizarTodaInterface() {
        atualizarTabelaParticipantes();
        atualizarCheckboxesParticipantes();
        atualizarTabelaDespesas();
        atualizarAbaResumo();
    }

    private void atualizarTabelaParticipantes() {
        modelParticipantes.setRowCount(0);
        for (Participante p : controller.listarParticipantes()) {
            modelParticipantes.addRow(new Object[]{
                    p.getCodigo(),
                    p.getNome(),
                    formatarMoeda(p.getOrcamento())
            });
        }
    }

    private void atualizarCheckboxesParticipantes() {
        List<Integer> marcadosAtuais = obterCodigosSelecionados();
        pnlCheckboxesParticipantes.removeAll();
        mapaCheckboxes.clear();

        for (Participante p : controller.listarParticipantes()) {
            JCheckBox cb = new JCheckBox(p.getCodigo() + " - " + p.getNome());
            cb.setSelected(marcadosAtuais.contains(p.getCodigo()));
            mapaCheckboxes.put(p.getCodigo(), cb);
            pnlCheckboxesParticipantes.add(cb);
        }

        pnlCheckboxesParticipantes.revalidate();
        pnlCheckboxesParticipantes.repaint();
    }

    private void atualizarTabelaDespesas() {
        modelDespesas.setRowCount(0);
        List<Despesa> despesas = controller.listarDespesas();
        for (int i = 0; i < despesas.size(); i++) {
            Despesa d = despesas.get(i);
            String classificacao = d.isEssencial() ? "Essencial" : "Opcional (Prio " + d.getPrioridade() + ")";

            List<String> nomes = new ArrayList<>();
            for (Participante p : d.getParticipantes()) {
                nomes.add(p.getNome());
            }

            modelDespesas.addRow(new Object[]{
                    i + 1,
                    d.getDescricao(),
                    d.getTipoDescricao(),
                    formatarMoeda(d.getValor()),
                    formatarMoeda(d.getCustoTotal()),
                    classificacao,
                    String.join(", ", nomes)
            });
        }
    }

    private void atualizarAbaResumo() {
        List<Participante> participantes = controller.listarParticipantes();
        List<Despesa> despesas = controller.listarDespesas();
        Map<Participante, BigDecimal> totais = controller.calcularTotaisIndividuais();
        Map<Participante, BigDecimal> excedentes = controller.calcularExcedentes();
        BigDecimal custoGeral = controller.calcularCustoTotalGeral();

        lblCustoTotalGeral.setText("Custo Total: " + formatarMoeda(custoGeral));
        lblQtdParticipantes.setText("Participantes: " + participantes.size());
        lblQtdDespesas.setText("Despesas: " + despesas.size());

        // 1. Tabela de situação individual
        modelResumoIndividual.setRowCount(0);
        int qtdExcedidos = 0;
        for (Participante p : participantes) {
            BigDecimal gasto = totais.get(p);
            BigDecimal exc = excedentes.get(p);
            String situacao;
            if (exc.compareTo(BigDecimal.ZERO) > 0) {
                situacao = "EXCEDE ORÇAMENTO (" + formatarMoeda(exc) + ")";
                qtdExcedidos++;
            } else if (gasto.compareTo(p.getOrcamento()) == 0 && gasto.compareTo(BigDecimal.ZERO) > 0) {
                situacao = "Dentro do orçamento (no limite)";
            } else {
                situacao = "Dentro do orçamento";
            }

            modelResumoIndividual.addRow(new Object[]{
                    p.getCodigo(),
                    p.getNome(),
                    formatarMoeda(p.getOrcamento()),
                    formatarMoeda(gasto),
                    formatarMoeda(exc),
                    situacao
            });
        }

        if (participantes.isEmpty()) {
            lblStatusGeral.setText("Situação: Sem dados");
            lblStatusGeral.setForeground(Color.DARK_GRAY);
        } else if (qtdExcedidos > 0) {
            lblStatusGeral.setText("Atenção: " + qtdExcedidos + " acima do limite");
            lblStatusGeral.setForeground(new Color(180, 0, 0));
        } else {
            lblStatusGeral.setText("Situação: Cabe no bolso de todos!");
            lblStatusGeral.setForeground(new Color(0, 120, 0));
        }

        // 2. Matriz detalhada (Pessoa x Despesa)
        List<String> colunasMatriz = new ArrayList<>();
        colunasMatriz.add("Código");
        colunasMatriz.add("Participante");
        for (Despesa d : despesas) {
            colunasMatriz.add(d.getDescricao());
        }
        colunasMatriz.add("Total Individual");

        modelMatrizRateio.setDataVector(new Object[0][0], colunasMatriz.toArray());

        for (Participante p : participantes) {
            Object[] linha = new Object[colunasMatriz.size()];
            linha[0] = p.getCodigo();
            linha[1] = p.getNome();

            for (int j = 0; j < despesas.size(); j++) {
                Map<Participante, BigDecimal> rateio = despesas.get(j).calcularRateio();
                BigDecimal valorNaDespesa = rateio.getOrDefault(p, BigDecimal.ZERO);
                linha[j + 2] = formatarMoeda(valorNaDespesa);
            }
            linha[colunasMatriz.size() - 1] = formatarMoeda(totais.get(p));
            modelMatrizRateio.addRow(linha);
        }
    }

    // =========================================================================
    // MÉTODOS AUXILIARES DE INTERFACE
    // =========================================================================
    private void carregarDespesaNoFormulario(int row) {
        Despesa d = controller.listarDespesas().get(row);
        txtDespDescricao.setText(d.getDescricao());
        txtDespValor.setText(d.getValor().toPlainString().replace(".", ","));
        cbDespTipo.setSelectedIndex("Fixa".equalsIgnoreCase(d.getTipoDescricao()) ? 0 : 1);
        chkDespEssencial.setSelected(d.isEssencial());
        spnDespPrioridade.setEnabled(!d.isEssencial());
        if (!d.isEssencial()) {
            spnDespPrioridade.setValue(d.getPrioridade());
        }

        mapaCheckboxes.values().forEach(cb -> cb.setSelected(false));
        for (Participante p : d.getParticipantes()) {
            JCheckBox cb = mapaCheckboxes.get(p.getCodigo());
            if (cb != null) {
                cb.setSelected(true);
            }
        }
    }

    private List<Integer> obterCodigosSelecionados() {
        List<Integer> codigos = new ArrayList<>();
        for (Map.Entry<Integer, JCheckBox> entry : mapaCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                codigos.add(entry.getKey());
            }
        }
        return codigos;
    }

    private Participante buscarParticipanteLocal(int codigo) {
        for (Participante p : controller.listarParticipantes()) {
            if (p.getCodigo() == codigo) {
                return p;
            }
        }
        return null;
    }

    private void limparCamposParticipante() {
        txtPartCodigo.setText("");
        txtPartCodigo.setEditable(true);
        txtPartNome.setText("");
        txtPartOrcamento.setText("");
        tblParticipantes.clearSelection();
    }

    private void limparCamposDespesa() {
        txtDespDescricao.setText("");
        txtDespValor.setText("");
        cbDespTipo.setSelectedIndex(0);
        chkDespEssencial.setSelected(true);
        spnDespPrioridade.setValue(1);
        spnDespPrioridade.setEnabled(false);
        mapaCheckboxes.values().forEach(cb -> cb.setSelected(false));
        tblDespesas.clearSelection();
    }

    private void limparTelaSimulador() {
        lblStatusSimulador.setText("Clique no botão acima para simular ajustes sem alterar o planejamento original.");
        lblStatusSimulador.setForeground(Color.BLACK);
        txtDetalhesSimulador.setText("");
        modelComparativoSimulador.setRowCount(0);
    }

    private BigDecimal parseValorMonetario(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("O campo de valor não pode ficar vazio.");
        }
        String limpo = texto.trim().replace("R$", "").replace(" ", "").replace(",", ".");
        try {
            return new BigDecimal(limpo);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor monetário inválido: '" + texto + "'. Use formato como 75,00.");
        }
    }

    private String formatarMoeda(BigDecimal valor) {
        return MOEDA_FMT.format(valor);
    }

    private JLabel criarLabelIndicador(String texto) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 8, 10, 8)
        ));
        return lbl;
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Aviso / Validação", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}