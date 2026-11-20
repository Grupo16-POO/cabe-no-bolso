package br.com.cabenobolso.view;

import br.com.cabenobolso.controller.PlanejamentoController;
import br.com.cabenobolso.model.Despesa;
import br.com.cabenobolso.model.OtimizadorDespesas;
import br.com.cabenobolso.model.Participante;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainFrame extends JFrame implements ActionListener, ListSelectionListener {
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
    private final PlanejamentoController controller = new PlanejamentoController();

    // Barra superior
    private JButton btnNovo, btnSalvar, btnCarregar, btnReferencia;

    // Aba 1: Participantes
    private final JTextField txtCod = new JTextField(6), txtNome = new JTextField(18), txtOrc = new JTextField(10);
    private JButton btnCadPart, btnEditPart, btnExcPart, btnLimpPart;
    private DefaultTableModel modPart;
    private JTable tblPart;

    // Aba 2: Despesas
    private final JTextField txtDesc = new JTextField(18), txtVal = new JTextField(9);
    private final JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Fixa (divide entre selecionados)", "Por pessoa (valor unitário)"});
    private final JCheckBox chkEssencial = new JCheckBox("Despesa Essencial (obrigatória no simulador)", true);
    private final JSpinner spnPrio = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    private final JPanel pnlChecks = new JPanel();
    private final Map<Integer, JCheckBox> mapaChecks = new LinkedHashMap<>();
    private JButton btnMarcarTodos, btnDesmarcarTodos, btnAddDesp, btnEditDesp, btnExcDesp, btnLimpDesp;
    private DefaultTableModel modDesp;
    private JTable tblDesp;

    // Aba 3: Resumo
    private JLabel lblResumoGeral;
    private DefaultTableModel modResumoInd, modMatriz;

    // Aba 4: Simulador (Bônus)
    private JButton btnSimular;
    private JLabel lblStatusSim;
    private JTextArea txtDetalhesSim;
    private DefaultTableModel modCompSim;

    public MainFrame() {
        setTitle("Cabe no Bolso - Planejador de Despesas Compartilhadas");
        setSize(1000, 680);
        setMinimumSize(new Dimension(880, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // Barra Superior
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        barra.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        btnNovo = criarBotao("Novo Planejamento (Vazio)", barra);
        btnSalvar = criarBotao("Salvar Planejamento (.dat)", barra);
        btnCarregar = criarBotao("Carregar Planejamento (.dat)", barra);
        btnReferencia = criarBotao("Carregar Caso de Referência (Seção 4)", barra);
        btnReferencia.setFont(new Font("SansSerif", Font.BOLD, 12));
        add(barra, BorderLayout.NORTH);

        // Abas Principais
        JTabbedPane abas = new JTabbedPane();
        abas.setFont(new Font("SansSerif", Font.BOLD, 13));
        abas.addTab("1. Participantes", montarAbaParticipantes());
        abas.addTab("2. Despesas", montarAbaDespesas());
        abas.addTab("3. Resumo do Planejamento", montarAbaResumo());
        abas.addTab("4. Simulador de Alternativas (Bônus)", montarAbaSimulador());
        add(abas, BorderLayout.CENTER);

        atualizarTelas();
    }

    private JPanel montarAbaParticipantes() {
        JPanel painel = painelBase();
        JPanel form = new JPanel(new GridLayout(2, 1, 4, 4));
        form.setBorder(new TitledBorder("Cadastro e Edição de Participante"));
        form.add(linha(new JLabel("Código Único:"), txtCod, new JLabel("Nome:"), txtNome, new JLabel("Orçamento (R$):"), txtOrc));

        JPanel botoes = linha();
        btnCadPart = criarBotao("Cadastrar", botoes);
        btnEditPart = criarBotao("Salvar Alterações do Selecionado", botoes);
        btnExcPart = criarBotao("Excluir Selecionado", botoes);
        btnLimpPart = criarBotao("Limpar Campos", botoes);
        form.add(botoes);

        modPart = novoModelo(new String[]{"Código", "Nome", "Orçamento Disponível"});
        tblPart = novaTabela(modPart);
        painel.add(form, BorderLayout.NORTH);
        painel.add(comBorda(new JScrollPane(tblPart), "Participantes Cadastrados (clique na linha para editar/excluir)"), BorderLayout.CENTER);
        return painel;
    }

    private JPanel montarAbaDespesas() {
        JPanel painel = painelBase();
        spnPrio.setEnabled(false);
        chkEssencial.addActionListener(this);

        JPanel campos = new JPanel(new GridLayout(4, 1, 2, 2));
        campos.add(linha(new JLabel("Descrição:"), txtDesc, new JLabel("Valor (R$):"), txtVal));
        campos.add(linha(new JLabel("Tipo de Rateio:"), cbTipo));
        campos.add(linha(chkEssencial, new JLabel("Prioridade (se opcional):"), spnPrio));

        JPanel botoes = linha();
        btnAddDesp = criarBotao("Adicionar Despesa", botoes);
        btnEditDesp = criarBotao("Salvar Alterações da Selecionada", botoes);
        btnExcDesp = criarBotao("Excluir Selecionada", botoes);
        btnLimpDesp = criarBotao("Limpar Campos", botoes);
        campos.add(botoes);

        pnlChecks.setLayout(new BoxLayout(pnlChecks, BoxLayout.Y_AXIS));
        JPanel direita = new JPanel(new BorderLayout(4, 4));
        direita.setBorder(new TitledBorder("Participantes Envolvidos"));
        direita.setPreferredSize(new Dimension(280, 150));
        direita.add(new JScrollPane(pnlChecks), BorderLayout.CENTER);

        JPanel rodapeChecks = linha();
        btnMarcarTodos = criarBotao("Marcar Todos", rodapeChecks);
        btnDesmarcarTodos = criarBotao("Desmarcar Todos", rodapeChecks);
        direita.add(rodapeChecks, BorderLayout.SOUTH);

        JPanel topo = new JPanel(new BorderLayout(8, 8));
        topo.setBorder(new TitledBorder("Cadastro e Edição de Despesa"));
        topo.add(campos, BorderLayout.CENTER);
        topo.add(direita, BorderLayout.EAST);

        modDesp = novoModelo(new String[]{"#", "Descrição", "Tipo", "Valor Informado", "Custo Total", "Classificação", "Participantes"});
        tblDesp = novaTabela(modDesp);
        painel.add(topo, BorderLayout.NORTH);
        painel.add(comBorda(new JScrollPane(tblDesp), "Despesas Lançadas (clique na linha para editar/excluir)"), BorderLayout.CENTER);
        return painel;
    }

    private JPanel montarAbaResumo() {
        JPanel painel = painelBase();
        lblResumoGeral = new JLabel("Custo Total: R$ 0,00 | Participantes: 0 | Despesas: 0", SwingConstants.CENTER);
        lblResumoGeral.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblResumoGeral.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), new EmptyBorder(10, 8, 10, 8)));

        modResumoInd = novoModelo(new String[]{"Código", "Participante", "Orçamento", "Total Individual", "Valor Excedido", "Situação"});
        modMatriz = novoModelo(new String[]{});

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                comBorda(new JScrollPane(novaTabela(modResumoInd)), "Situação Individual vs. Orçamento"),
                comBorda(new JScrollPane(novaTabela(modMatriz)), "Detalhamento do Rateio por Despesa (Valores em R$)"));
        split.setResizeWeight(0.5);

        painel.add(lblResumoGeral, BorderLayout.NORTH);
        painel.add(split, BorderLayout.CENTER);
        return painel;
    }

    private JPanel montarAbaSimulador() {
        JPanel painel = painelBase();
        JPanel topo = new JPanel(new BorderLayout(8, 8));
        btnSimular = criarBotao("Executar Simulação de Alternativas (Manter Essenciais + Maximizar Prioridade)", null);
        btnSimular.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatusSim = new JLabel("Clique no botão acima para simular ajustes sem alterar o planejamento original.");
        lblStatusSim.setFont(new Font("SansSerif", Font.BOLD, 13));
        topo.add(btnSimular, BorderLayout.NORTH);
        topo.add(lblStatusSim, BorderLayout.SOUTH);

        txtDetalhesSim = new JTextArea(8, 40);
        txtDetalhesSim.setEditable(false);
        txtDetalhesSim.setFont(new Font("Monospaced", Font.PLAIN, 13));
        modCompSim = novoModelo(new String[]{"Código", "Participante", "Orçamento", "Gasto Original", "Gasto Simulado", "Nova Situação"});

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                comBorda(new JScrollPane(txtDetalhesSim), "Despesas Mantidas e Retiradas na Simulação"),
                comBorda(new JScrollPane(novaTabela(modCompSim)), "Comparativo Individual (Antes x Depois da Simulação)"));
        split.setResizeWeight(0.45);

        painel.add(topo, BorderLayout.NORTH);
        painel.add(split, BorderLayout.CENTER);
        return painel;
    }

    // =========================================================================
    // EVENTOS DE CLIQUE E SELEÇÃO (SEM LAMBDAS)
    // =========================================================================
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == chkEssencial) {
            spnPrio.setEnabled(!chkEssencial.isSelected());
        } else if (src == btnNovo) {
            if (JOptionPane.showConfirmDialog(this, "Iniciar planejamento vazio? Dados não salvos serão perdidos.", "Novo", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                controller.novoPlanejamento();
                limparTudo();
            }
        } else if (src == btnSalvar) {
            gerenciarArquivo(true);
        } else if (src == btnCarregar) {
            gerenciarArquivo(false);
        } else if (src == btnReferencia) {
            controller.carregarCasoReferencia();
            limparTudo();
            JOptionPane.showMessageDialog(this, "Caso de referência carregado (Ana, Bruno e Carla - Total R$ 210,00).");
        } else if (src == btnCadPart) {
            salvarParticipante(false);
        } else if (src == btnEditPart) {
            salvarParticipante(true);
        } else if (src == btnExcPart) {
            excluirItem(true);
        } else if (src == btnLimpPart) {
            limparPart();
        } else if (src == btnMarcarTodos || src == btnDesmarcarTodos) {
            for (JCheckBox cb : mapaChecks.values()) cb.setSelected(src == btnMarcarTodos);
        } else if (src == btnAddDesp) {
            salvarDespesa(false);
        } else if (src == btnEditDesp) {
            salvarDespesa(true);
        } else if (src == btnExcDesp) {
            excluirItem(false);
        } else if (src == btnLimpDesp) {
            limparDesp();
        } else if (src == btnSimular) {
            executarSimulador();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        if (e.getSource() == tblPart.getSelectionModel() && tblPart.getSelectedRow() >= 0) {
            int cod = (int) modPart.getValueAt(tblPart.getSelectedRow(), 0);
            for (Participante p : controller.listarParticipantes()) {
                if (p.getCodigo() == cod) {
                    txtCod.setText(String.valueOf(p.getCodigo()));
                    txtCod.setEditable(false);
                    txtNome.setText(p.getNome());
                    txtOrc.setText(p.getOrcamento().toPlainString().replace(".", ","));
                    break;
                }
            }
        } else if (e.getSource() == tblDesp.getSelectionModel() && tblDesp.getSelectedRow() >= 0) {
            Despesa d = controller.listarDespesas().get(tblDesp.getSelectedRow());
            txtDesc.setText(d.getDescricao());
            txtVal.setText(d.getValor().toPlainString().replace(".", ","));
            cbTipo.setSelectedIndex("Fixa".equalsIgnoreCase(d.getTipoDescricao()) ? 0 : 1);
            chkEssencial.setSelected(d.isEssencial());
            spnPrio.setEnabled(!d.isEssencial());
            if (!d.isEssencial()) spnPrio.setValue(d.getPrioridade());
            for (JCheckBox cb : mapaChecks.values()) cb.setSelected(false);
            for (Participante p : d.getParticipantes()) {
                if (mapaChecks.containsKey(p.getCodigo())) mapaChecks.get(p.getCodigo()).setSelected(true);
            }
        }
    }

    // =========================================================================
    // OPERAÇÕES DO SISTEMA
    // =========================================================================
    private void gerenciarArquivo(boolean salvar) {
        JFileChooser fc = new JFileChooser(new File("data"));
        fc.setFileFilter(new FileNameExtensionFilter("Planejamento (*.dat)", "dat"));
        if (salvar) fc.setSelectedFile(new File("meu-planejamento.dat"));

        int op = salvar ? fc.showSaveDialog(this) : fc.showOpenDialog(this);
        if (op == JFileChooser.APPROVE_OPTION) {
            try {
                String path = fc.getSelectedFile().getAbsolutePath();
                if (salvar) {
                    if (!path.toLowerCase().endsWith(".dat")) path += ".dat";
                    controller.salvarPlanejamento(path);
                    JOptionPane.showMessageDialog(this, "Salvo com sucesso em:\n" + path);
                } else {
                    controller.carregarPlanejamento(path);
                    limparTudo();
                    JOptionPane.showMessageDialog(this, "Planejamento carregado com sucesso!");
                }
            } catch (Exception ex) {
                erro("Erro no arquivo (dados atuais preservados):\n" + ex.getMessage());
            }
        }
    }

    private void salvarParticipante(boolean editar) {
        int row = tblPart.getSelectedRow();
        if (editar && row < 0) { erro("Selecione um participante na tabela."); return; }
        try {
            int cod = editar ? (int) modPart.getValueAt(row, 0) : Integer.parseInt(txtCod.getText().trim());
            BigDecimal orc = parseValor(txtOrc.getText());
            if (editar) controller.editarParticipante(cod, txtNome.getText(), orc);
            else controller.cadastrarParticipante(cod, txtNome.getText(), orc);
            limparPart();
            atualizarTelas();
        } catch (NumberFormatException ex) {
            erro("Código deve ser inteiro e orçamento numérico (ex: 70,00).");
        } catch (Exception ex) {
            erro(ex.getMessage());
        }
    }

    private void salvarDespesa(boolean editar) {
        int row = tblDesp.getSelectedRow();
        if (editar && row < 0) { erro("Selecione uma despesa na tabela."); return; }
        try {
            String tipo = cbTipo.getSelectedIndex() == 0 ? "FIXA" : "POR_PESSOA";
            BigDecimal val = parseValor(txtVal.getText());
            boolean ess = chkEssencial.isSelected();
            int prio = ess ? 0 : (Integer) spnPrio.getValue();
            List<Integer> cods = new ArrayList<>();
            for (Map.Entry<Integer, JCheckBox> item : mapaChecks.entrySet()) {
                if (item.getValue().isSelected()) cods.add(item.getKey());
            }
            if (editar) controller.editarDespesa(row, tipo, txtDesc.getText(), val, cods, ess, prio);
            else controller.cadastrarDespesa(tipo, txtDesc.getText(), val, cods, ess, prio);
            limparDesp();
            atualizarTelas();
        } catch (Exception ex) {
            erro(ex.getMessage());
        }
    }

    private void excluirItem(boolean participante) {
        JTable tbl = participante ? tblPart : tblDesp;
        if (tbl.getSelectedRow() < 0) { erro("Selecione um item na tabela para excluir."); return; }
        try {
            if (participante) {
                controller.excluirParticipante((int) modPart.getValueAt(tbl.getSelectedRow(), 0));
                limparPart();
            } else {
                controller.excluirDespesa(tbl.getSelectedRow());
                limparDesp();
            }
            atualizarTelas();
        } catch (Exception ex) {
            erro(ex.getMessage());
        }
    }

    private void executarSimulador() {
        try {
            OtimizadorDespesas.ResultadoOtimizacao res = controller.executarSimulador();
            Map<Participante, BigDecimal> originais = controller.calcularTotaisIndividuais();
            modCompSim.setRowCount(0);
            StringBuilder sb = new StringBuilder();

            if (!res.isViavel()) {
                lblStatusSim.setText("SEM SOLUÇÃO VIÁVEL (Essenciais excedem o orçamento)");
                lblStatusSim.setForeground(new Color(180, 0, 0));
                sb.append("MOTIVO:\n").append(res.getMensagem()).append("\n\nO planejamento original permaneceu intacto.");
            } else {
                lblStatusSim.setText("SOLUÇÃO VIÁVEL! Novo Custo Geral: " + MOEDA.format(res.getNovoCustoTotalGeral()) + " (Prioridade: " + res.getSomaPrioridades() + ")");
                lblStatusSim.setForeground(new Color(0, 120, 0));
                sb.append("=== MANTIDAS ===\n");
                for (Despesa d : res.getDespesasMantidas()) {
                    sb.append(" • ").append(d.getDescricao()).append(" (").append(MOEDA.format(d.getCustoTotal())).append(")\n");
                }
                sb.append("\n=== RETIRADAS ===\n");
                if (res.getDespesasRetiradas().isEmpty()) sb.append(" • Nenhuma despesa retirada.\n");
                for (Despesa d : res.getDespesasRetiradas()) {
                    sb.append(" • ").append(d.getDescricao()).append(" [Prio ").append(d.getPrioridade()).append("] (-").append(MOEDA.format(d.getCustoTotal())).append(")\n");
                }
            }

            for (Participante p : controller.listarParticipantes()) {
                BigDecimal sim = res.getNovosTotaisIndividuais().get(p);
                String sit = sim.compareTo(p.getOrcamento()) <= 0 ? "Dentro do orçamento" : "Excede " + MOEDA.format(sim.subtract(p.getOrcamento()));
                modCompSim.addRow(new Object[]{p.getCodigo(), p.getNome(), MOEDA.format(p.getOrcamento()), MOEDA.format(originais.get(p)), MOEDA.format(sim), sit});
            }
            txtDetalhesSim.setText(sb.toString());
        } catch (Exception ex) {
            erro(ex.getMessage());
        }
    }

    // =========================================================================
    // ATUALIZAÇÃO E AUXILIARES
    // =========================================================================
    private void atualizarTelas() {
        modPart.setRowCount(0);
        pnlChecks.removeAll();
        mapaChecks.clear();
        List<Participante> parts = controller.listarParticipantes();
        for (Participante p : parts) {
            modPart.addRow(new Object[]{p.getCodigo(), p.getNome(), MOEDA.format(p.getOrcamento())});
            JCheckBox cb = new JCheckBox(p.getCodigo() + " - " + p.getNome());
            mapaChecks.put(p.getCodigo(), cb);
            pnlChecks.add(cb);
        }
        pnlChecks.revalidate();
        pnlChecks.repaint();

        modDesp.setRowCount(0);
        List<Despesa> desps = controller.listarDespesas();
        for (int i = 0; i < desps.size(); i++) {
            Despesa d = desps.get(i);
            List<String> nomes = new ArrayList<>();
            for (Participante p : d.getParticipantes()) nomes.add(p.getNome());
            modDesp.addRow(new Object[]{i + 1, d.getDescricao(), d.getTipoDescricao(), MOEDA.format(d.getValor()),
                    MOEDA.format(d.getCustoTotal()), d.isEssencial() ? "Essencial" : "Opcional (Prio " + d.getPrioridade() + ")", String.join(", ", nomes)});
        }

        Map<Participante, BigDecimal> totais = controller.calcularTotaisIndividuais();
        Map<Participante, BigDecimal> excs = controller.calcularExcedentes();
        modResumoInd.setRowCount(0);
        int acima = 0;
        for (Participante p : parts) {
            BigDecimal g = totais.get(p), ex = excs.get(p);
            if (ex.compareTo(BigDecimal.ZERO) > 0) acima++;
            String sit = ex.compareTo(BigDecimal.ZERO) > 0 ? "EXCEDE (" + MOEDA.format(ex) + ")"
                    : (g.compareTo(p.getOrcamento()) == 0 && g.compareTo(BigDecimal.ZERO) > 0 ? "No limite" : "Dentro do orçamento");
            modResumoInd.addRow(new Object[]{p.getCodigo(), p.getNome(), MOEDA.format(p.getOrcamento()), MOEDA.format(g), MOEDA.format(ex), sit});
        }

        lblResumoGeral.setText("Custo Total: " + MOEDA.format(controller.calcularCustoTotalGeral()) + "  |  Participantes: " + parts.size()
                + "  |  Despesas: " + desps.size() + "  |  " + (acima > 0 ? "Atenção: " + acima + " acima do limite" : "Situação: OK"));
        lblResumoGeral.setForeground(acima > 0 ? new Color(180, 0, 0) : new Color(0, 120, 0));

        List<String> cols = new ArrayList<>();
        cols.add("Código");
        cols.add("Participante");
        for (Despesa d : desps) cols.add(d.getDescricao());
        cols.add("Total Individual");
        modMatriz.setDataVector(new Object[0][0], cols.toArray());
        for (Participante p : parts) {
            Object[] linha = new Object[cols.size()];
            linha[0] = p.getCodigo();
            linha[1] = p.getNome();
            for (int j = 0; j < desps.size(); j++) linha[j + 2] = MOEDA.format(desps.get(j).calcularRateio().getOrDefault(p, BigDecimal.ZERO));
            linha[cols.size() - 1] = MOEDA.format(totais.get(p));
            modMatriz.addRow(linha);
        }
    }

    private JPanel painelBase() { JPanel p = new JPanel(new BorderLayout(10, 10)); p.setBorder(new EmptyBorder(10, 10, 10, 10)); return p; }
    private JPanel linha(Component... cs) { JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3)); for (Component c : cs) p.add(c); return p; }
    private JComponent comBorda(JComponent c, String t) { c.setBorder(new TitledBorder(t)); return c; }
    private JButton criarBotao(String t, Container pai) { JButton b = new JButton(t); b.addActionListener(this); if (pai != null) pai.add(b); return b; }
    private DefaultTableModel novoModelo(String[] cols) { return new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } }; }
    private JTable novaTabela(DefaultTableModel m) { JTable t = new JTable(m); t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); t.setRowHeight(24); t.getSelectionModel().addListSelectionListener(this); return t; }

    private void limparTudo() { limparPart(); limparDesp(); txtDetalhesSim.setText(""); modCompSim.setRowCount(0); atualizarTelas(); }
    private void limparPart() { txtCod.setText(""); txtCod.setEditable(true); txtNome.setText(""); txtOrc.setText(""); tblPart.clearSelection(); }
    private void limparDesp() { txtDesc.setText(""); txtVal.setText(""); cbTipo.setSelectedIndex(0); chkEssencial.setSelected(true); spnPrio.setValue(1); spnPrio.setEnabled(false); tblDesp.clearSelection(); }
    private void erro(String msg) { JOptionPane.showMessageDialog(this, msg, "Aviso / Validação", JOptionPane.ERROR_MESSAGE); }

    private BigDecimal parseValor(String txt) {
        if (txt == null || txt.trim().isEmpty()) throw new IllegalArgumentException("O campo de valor não pode ficar vazio.");
        try { return new BigDecimal(txt.trim().replace("R$", "").replace(" ", "").replace(",", ".")); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Valor inválido: '" + txt + "'. Use formato como 75,00."); }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(new Runnable() { public void run() { new MainFrame().setVisible(true); } });
    }
}