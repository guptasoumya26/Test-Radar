package com.testradar.ui;

import com.testradar.AppContext;
import com.testradar.model.CoverageReport;
import com.testradar.ui.components.DonutChart;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class CoveragePanel extends JPanel {

    private final AppContext ctx;
    private final DonutChart donut = new DonutChart();
    private final JLabel sourceClasses = statValue();
    private final JLabel coveredClasses = statValue();
    private final JLabel testClasses = statValue();
    private final JLabel totalTests = statValue();
    private final CoverageTableModel model = new CoverageTableModel();
    private final JTable table = new JTable(model);

    public CoveragePanel(AppContext ctx) {
        this.ctx = ctx;
        setBorder(UiUtils.pad(20));
        setLayout(new BorderLayout(0, 14));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        ctx.addSuiteLoadedListener(this::refresh);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.add(UiUtils.title("Suite Coverage"), BorderLayout.NORTH);
        header.add(UiUtils.subtitle("Heuristic estimate: which production classes are exercised by the test suite. "
                + "(Static analysis - no test execution.)"), BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(16, 16));

        JPanel left = new JPanel(new BorderLayout(0, 14));
        JPanel donutCard = UiUtils.card();
        donutCard.setLayout(new BorderLayout());
        donut.setForeground(getForeground());
        donutCard.add(donut, BorderLayout.CENTER);
        donutCard.add(new JLabel("Class coverage", JLabel.CENTER), BorderLayout.SOUTH);
        left.add(donutCard, BorderLayout.CENTER);

        JPanel stats = new JPanel(new GridLayout(2, 2, 12, 12));
        stats.add(statCard("Source classes", sourceClasses));
        stats.add(statCard("Covered classes", coveredClasses));
        stats.add(statCard("Test classes", testClasses));
        stats.add(statCard("Test methods", totalTests));
        left.add(stats, BorderLayout.SOUTH);
        left.setPreferredSize(new java.awt.Dimension(300, 100));
        body.add(left, BorderLayout.WEST);

        table.setRowHeight(24);
        table.getColumnModel().getColumn(1).setMaxWidth(110);
        table.getColumnModel().getColumn(2).setMaxWidth(110);
        table.getColumnModel().getColumn(1).setCellRenderer(new CoveredRenderer());
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Per-class coverage"));
        body.add(scroll, BorderLayout.CENTER);

        return body;
    }

    private JPanel statCard(String label, JLabel value) {
        JPanel card = UiUtils.card();
        card.setLayout(new BorderLayout());
        JLabel l = new JLabel(label.toUpperCase());
        l.setForeground(new Color(0x6B7280));
        l.setFont(l.getFont().deriveFont(10.5f));
        card.add(value, BorderLayout.CENTER);
        card.add(l, BorderLayout.SOUTH);
        return card;
    }

    private static JLabel statValue() {
        JLabel l = new JLabel("-");
        l.setFont(l.getFont().deriveFont(Font.BOLD, 22f));
        return l;
    }

    private void refresh() {
        CoverageReport r = ctx.getCoverageReport();
        if (r == null) return;
        donut.setForeground(getForeground());
        donut.setValue(r.getCoveragePercent(), r.getCoveredSourceClasses() + " / " + r.getTotalSourceClasses() + " classes");
        sourceClasses.setText(String.valueOf(r.getTotalSourceClasses()));
        coveredClasses.setText(String.valueOf(r.getCoveredSourceClasses()));
        testClasses.setText(String.valueOf(r.getTotalTestClasses()));
        totalTests.setText(String.valueOf(r.getTotalTests()));
        model.setData(r.getDetails());
    }

    private static class CoverageTableModel extends AbstractTableModel {
        private final String[] cols = {"Production class", "Covered", "Tests"};
        private List<CoverageReport.ClassCoverage> data = new ArrayList<>();

        void setData(List<CoverageReport.ClassCoverage> data) {
            this.data = data;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            CoverageReport.ClassCoverage cc = data.get(r);
            return switch (c) {
                case 0 -> cc.getClassName();
                case 1 -> cc.isCovered() ? "Yes" : "No";
                case 2 -> cc.getReferencingTests();
                default -> "";
            };
        }
    }

    private static class CoveredRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            setHorizontalAlignment(CENTER);
            if (!sel) {
                setForeground("Yes".equals(v) ? new Color(0x16A34A) : new Color(0xDC2626));
            }
            return this;
        }
    }
}
