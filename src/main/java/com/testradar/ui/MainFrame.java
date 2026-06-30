package com.testradar.ui;

import com.testradar.AppContext;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.awt.Dimension;

public class MainFrame extends JFrame {

    public MainFrame(AppContext ctx) {
        super("Test Radar - AI Test Impact Analysis");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setSize(new Dimension(1180, 760));
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP);
        tabs.putClientProperty("JTabbedPane.tabAreaAlignment", "leading");
        tabs.addTab("  Scan Repository  ", new ScanPanel(ctx));
        tabs.addTab("  Analyse Change  ", new AnalyzePanel(ctx));
        tabs.addTab("  Coverage  ", new CoveragePanel(ctx));
        tabs.addTab("  AI Chat  ", new ChatPanel(ctx));
        tabs.addTab("  Settings  ", new SettingsPanel(ctx));

        setContentPane(tabs);
    }
}
