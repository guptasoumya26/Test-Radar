package com.testradar.ui;

import com.testradar.AppContext;
import com.testradar.model.TestCase;
import com.testradar.service.JiraService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AnalyzePanel extends JPanel {

    private final AppContext ctx;

    private final JTextField jiraKeyField = new JTextField(12);
    private final JButton fetchJiraButton = new JButton("Fetch from Jira");
    private final JTextArea changeArea = new JTextArea(6, 40);
    private final JButton analyzeButton = new JButton("Analyse the Change");
    private final JButton selectAllButton = new JButton("Select all");
    private final JButton clearButton = new JButton("Clear selection");
    private final JButton exportButton = new JButton("Export testng.xml");
    private final JProgressBar progress = new JProgressBar();
    private final JLabel summary = new JLabel("Scan a repository first, then describe a change to analyse.");

    private final RelevantTableModel model = new RelevantTableModel();
    private final JTable table = new JTable(model);

    public AnalyzePanel(AppContext ctx) {
        this.ctx = ctx;
        setBorder(UiUtils.pad(20));
        setLayout(new BorderLayout(0, 14));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        wire();
        ctx.addSuiteLoadedListener(() -> {
            model.setData(new ArrayList<>());
            summary.setText("Suite loaded (" + ctx.getTestCases().size()
                    + " tests). Describe a change and click Analyse.");
        });
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.add(UiUtils.title("Analyse Change Impact"), BorderLayout.NORTH);
        header.add(UiUtils.subtitle("AI identifies which existing tests are relevant to your upcoming change."),
                BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 14));

        JPanel input = UiUtils.card();
        input.setLayout(new BorderLayout(0, 10));

        JPanel jiraRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        jiraRow.add(new JLabel("Jira issue:"));
        jiraKeyField.putClientProperty("JTextField.placeholderText", "e.g. PROJ-123");
        jiraRow.add(jiraKeyField);
        jiraRow.add(fetchJiraButton);
        input.add(jiraRow, BorderLayout.NORTH);

        changeArea.setLineWrap(true);
        changeArea.setWrapStyleWord(true);
        changeArea.putClientProperty("JTextArea.placeholderText",
                "Describe the upcoming change: which classes/methods/modules are affected, the nature of the change, etc.");
        JScrollPane changeScroll = new JScrollPane(changeArea);
        changeScroll.setBorder(BorderFactory.createTitledBorder("Upcoming change"));
        input.add(changeScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        UiUtils.primary(analyzeButton);
        actions.add(analyzeButton);
        progress.setVisible(false);
        progress.setIndeterminate(true);
        actions.add(progress);
        input.add(actions, BorderLayout.SOUTH);

        body.add(input, BorderLayout.NORTH);

        JPanel results = new JPanel(new BorderLayout(0, 8));
        results.add(summary, BorderLayout.NORTH);

        setupTable();
        results.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel resultActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        resultActions.add(selectAllButton);
        resultActions.add(clearButton);
        UiUtils.primary(exportButton);
        resultActions.add(exportButton);
        results.add(resultActions, BorderLayout.SOUTH);

        body.add(results, BorderLayout.CENTER);
        return body;
    }

    private void setupTable() {
        table.setRowHeight(26);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getTableHeader().setReorderingAllowed(false);

        TableColumn sel = table.getColumnModel().getColumn(0);
        sel.setMaxWidth(50);
        sel.setMinWidth(50);

        TableColumn score = table.getColumnModel().getColumn(1);
        score.setMaxWidth(80);
        score.setMinWidth(70);
        score.setCellRenderer(new ScoreRenderer());

        table.getColumnModel().getColumn(2).setPreferredWidth(280);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(420);
    }

    private void wire() {
        fetchJiraButton.addActionListener(e -> fetchJira());
        analyzeButton.addActionListener(e -> analyze());
        selectAllButton.addActionListener(e -> model.selectAll(true));
        clearButton.addActionListener(e -> model.selectAll(false));
        exportButton.addActionListener(e -> export());
    }

    private void fetchJira() {
        String key = jiraKeyField.getText().trim();
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a Jira issue key first (e.g. PROJ-123).");
            return;
        }
        if (!ctx.getSettings().hasJira()) {
            JOptionPane.showMessageDialog(this,
                    "Jira is not configured. Add base URL, email and API token in the Settings tab.",
                    "Jira not configured", JOptionPane.WARNING_MESSAGE);
            return;
        }
        fetchJiraButton.setEnabled(false);
        new SwingWorker<JiraService.JiraIssue, Void>() {
            Exception error;
            @Override protected JiraService.JiraIssue doInBackground() {
                try { return ctx.getJiraService().fetchIssue(key); }
                catch (Exception ex) { error = ex; return null; }
            }
            @Override protected void done() {
                fetchJiraButton.setEnabled(true);
                if (error != null) {
                    JOptionPane.showMessageDialog(AnalyzePanel.this, error.getMessage(),
                            "Jira error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JiraService.JiraIssue issue = null;
                try { issue = get(); } catch (Exception ignored) {}
                if (issue != null) changeArea.setText(issue.asChangeText());
            }
        }.execute();
    }

    private void analyze() {
        if (!ctx.hasSuite()) {
            JOptionPane.showMessageDialog(this, "Scan and analyse a repository first (Scan tab).");
            return;
        }
        String change = changeArea.getText().trim();
        if (change.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Describe the upcoming change (or fetch it from Jira).");
            return;
        }
        if (!ctx.getSettings().hasApiKey()) {
            JOptionPane.showMessageDialog(this, "Add your OpenAI API key in the Settings tab first.");
            return;
        }
        analyzeButton.setEnabled(false);
        progress.setVisible(true);
        summary.setText("Analysing change impact with AI…");

        List<TestCase> all = ctx.getTestCases();
        new SwingWorker<String, Void>() {
            Exception error;
            @Override protected String doInBackground() {
                try {
                    return ctx.getOpenAiService().analyzeRelevance(
                            change, all, ctx.getSuiteAnalysis().getTestSourceText());
                } catch (Exception ex) { error = ex; return null; }
            }
            @Override protected void done() {
                analyzeButton.setEnabled(true);
                progress.setVisible(false);
                if (error != null) {
                    summary.setText(" ");
                    JOptionPane.showMessageDialog(AnalyzePanel.this, error.getMessage(),
                            "Analysis error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String resultSummary = "";
                try { resultSummary = get(); } catch (Exception ignored) {}

                List<TestCase> relevant = all.stream()
                        .filter(tc -> tc.getRelevanceScore() >= 0.4)
                        .sorted(Comparator.comparingDouble(TestCase::getRelevanceScore).reversed())
                        .collect(Collectors.toList());
                model.setData(relevant);
                summary.setText("<html><b>" + relevant.size() + " relevant tests.</b> "
                        + escapeHtml(resultSummary) + "</html>");
            }
        }.execute();
    }

    private void export() {
        List<TestCase> selected = model.getSelected();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select at least one test to export.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export testng.xml");
        chooser.setSelectedFile(new File("testng.xml"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            File file = chooser.getSelectedFile();
            ctx.getExporter().writeTo(file.toPath(), selected, "Test Radar - Relevant Suite");
            JOptionPane.showMessageDialog(this, "Exported " + selected.size()
                    + " tests to:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static class RelevantTableModel extends AbstractTableModel {
        private final String[] cols = {"Run", "Score", "Test", "Framework", "Why it's relevant"};
        private List<TestCase> data = new ArrayList<>();

        void setData(List<TestCase> data) {
            this.data = data;
            fireTableDataChanged();
        }

        void selectAll(boolean value) {
            for (TestCase tc : data) tc.setSelected(value);
            fireTableDataChanged();
        }

        List<TestCase> getSelected() {
            List<TestCase> out = new ArrayList<>();
            for (TestCase tc : data) if (tc.isSelected()) out.add(tc);
            return out;
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override public Class<?> getColumnClass(int c) {
            if (c == 0) return Boolean.class;
            if (c == 1) return Double.class;
            return String.class;
        }

        @Override public boolean isCellEditable(int r, int c) { return c == 0; }

        @Override public Object getValueAt(int r, int c) {
            TestCase tc = data.get(r);
            return switch (c) {
                case 0 -> tc.isSelected();
                case 1 -> tc.getRelevanceScore();
                case 2 -> tc.getDisplayName();
                case 3 -> tc.getFramework();
                case 4 -> tc.getRelevanceReason();
                default -> "";
            };
        }

        @Override public void setValueAt(Object value, int r, int c) {
            if (c == 0) {
                data.get(r).setSelected(Boolean.TRUE.equals(value));
                fireTableCellUpdated(r, c);
            }
        }
    }

    private static class ScoreRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            double score = v instanceof Double ? (Double) v : 0;
            setText(String.format("%.2f", score));
            setHorizontalAlignment(CENTER);
            if (!sel) {
                if (score >= 0.75) setForeground(new Color(0x16A34A));
                else if (score >= 0.55) setForeground(new Color(0xD97706));
                else setForeground(new Color(0x6B7280));
            }
            return this;
        }
    }
}
