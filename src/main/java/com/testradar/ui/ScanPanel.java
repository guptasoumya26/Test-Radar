package com.testradar.ui;

import com.testradar.AppContext;
import com.testradar.model.CoverageReport;
import com.testradar.model.RepoInfo;
import com.testradar.model.SuiteAnalysis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class ScanPanel extends JPanel {

    private final AppContext ctx;
    private final JTextField sourceField = new JTextField();
    private final JButton scanButton = new JButton("Scan & Analyse");
    private final JButton browseButton = new JButton("Browse…");
    private final JProgressBar progressBar = new JProgressBar();
    private final JTextArea log = new JTextArea();

    private final JLabel javaFiles = statValue();
    private final JLabel testFiles = statValue();
    private final JLabel testMethods = statValue();
    private final JLabel coverageStat = statValue();

    public ScanPanel(AppContext ctx) {
        this.ctx = ctx;
        setBorder(UiUtils.pad(20));
        setLayout(new BorderLayout(0, 16));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        wire();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BorderLayout(0, 4));
        header.add(UiUtils.title("Scan Repository"), BorderLayout.NORTH);
        header.add(UiUtils.subtitle("Clone a GitHub repository or open a local project, then discover its Java test suite."),
                BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 16));

        JPanel inputCard = UiUtils.card();
        inputCard.setLayout(new BorderLayout(10, 10));
        sourceField.putClientProperty("JTextField.placeholderText",
                "https://github.com/owner/repo.git  or  D:\\path\\to\\local\\project");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(browseButton);
        UiUtils.primary(scanButton);
        buttons.add(scanButton);
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.add(new JLabel("Repository:"), BorderLayout.WEST);
        row.add(sourceField, BorderLayout.CENTER);
        inputCard.add(row, BorderLayout.NORTH);
        inputCard.add(buttons, BorderLayout.SOUTH);
        center.add(inputCard, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.add(buildStats(), BorderLayout.NORTH);

        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.putClientProperty("FlatLaf.styleClass", "monospaced");
        JScrollPane logScroll = new JScrollPane(log);
        logScroll.setBorder(BorderFactory.createTitledBorder("Activity"));
        body.add(logScroll, BorderLayout.CENTER);

        progressBar.setVisible(false);
        body.add(progressBar, BorderLayout.SOUTH);

        center.add(body, BorderLayout.CENTER);
        return center;
    }

    private JPanel buildStats() {
        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
        stats.add(statCard("Java files", javaFiles));
        stats.add(statCard("Test files", testFiles));
        stats.add(statCard("Test methods", testMethods));
        stats.add(statCard("Suite coverage", coverageStat));
        return stats;
    }

    private JPanel statCard(String label, JLabel value) {
        JPanel card = UiUtils.card();
        card.setLayout(new BorderLayout());
        JLabel l = new JLabel(label.toUpperCase());
        l.setForeground(new java.awt.Color(0x6B7280));
        l.setFont(l.getFont().deriveFont(11f));
        card.add(value, BorderLayout.CENTER);
        card.add(l, BorderLayout.SOUTH);
        return card;
    }

    private static JLabel statValue() {
        JLabel l = new JLabel("-");
        l.setFont(l.getFont().deriveFont(java.awt.Font.BOLD, 26f));
        return l;
    }

    private void wire() {
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Select a local project folder");
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                sourceField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        scanButton.addActionListener(e -> startScan());
        sourceField.addActionListener(e -> startScan());
    }

    private void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            log.append(msg + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });
    }

    private void startScan() {
        String source = sourceField.getText().trim();
        if (source.isEmpty()) {
            appendLog("Please enter a repository URL or local folder path.");
            return;
        }
        scanButton.setEnabled(false);
        browseButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        log.setText("");

        new SwingWorker<Void, Void>() {
            RepoInfo repo;
            SuiteAnalysis analysis;
            CoverageReport coverage;
            Exception error;

            @Override
            protected Void doInBackground() {
                try {
                    repo = ctx.getGitService().acquire(source, ScanPanel.this::appendLog);
                    appendLog("Analysing Java sources…");
                    analysis = ctx.getAnalyzer().analyze(repo, ScanPanel.this::appendLog);
                    coverage = ctx.getCoverageService().compute(analysis);
                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                scanButton.setEnabled(true);
                browseButton.setEnabled(true);
                if (error != null) {
                    appendLog("ERROR: " + error.getMessage());
                    return;
                }
                ctx.setRepoInfo(repo);
                ctx.setSuiteAnalysis(analysis);
                ctx.setCoverageReport(coverage);

                javaFiles.setText(String.valueOf(repo.getJavaFileCount()));
                testFiles.setText(String.valueOf(repo.getTestFileCount()));
                testMethods.setText(String.valueOf(analysis.getTestCases().size()));
                coverageStat.setText(String.format("%.0f%%", coverage.getCoveragePercent()));

                appendLog("Done. Discovered " + analysis.getTestCases().size()
                        + " tests; estimated coverage " + String.format("%.1f%%", coverage.getCoveragePercent())
                        + ". Open the other tabs to analyse changes, view coverage, or chat.");
                ctx.fireSuiteLoaded();
            }
        }.execute();
    }
}
