package com.testradar.ui;

import com.testradar.ui.components.NeuralNetworkPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.function.Consumer;

public class SplashScreen extends JFrame {

    private static final String[] FEATURES = {
            "Scan any GitHub repo or local Java project",
            "Discover JUnit & TestNG test cases automatically",
            "AI change-impact analysis - find the tests that matter",
            "Pull upcoming changes straight from Jira issues",
            "Visualise overall suite coverage at a glance",
            "Chat with AI about your test suite",
            "Export a ready-to-run testng.xml for selected tests",
    };

    private final NeuralNetworkPanel background = new NeuralNetworkPanel();

    public SplashScreen(Consumer<SplashScreen> onStart) {
        super("Test Radar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(900, 600);
        setLocationRelativeTo(null);

        background.setLayout(new GridBagLayout());
        background.setBorder(BorderFactory.createLineBorder(new Color(77, 163, 255, 90), 1));
        background.add(buildContent(onStart));
        setContentPane(background);
    }

    private JPanel buildContent(Consumer<SplashScreen> onStart) {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));

        JLabel logo = new JLabel("◎  TEST RADAR");
        logo.setForeground(Color.WHITE);
        logo.setFont(logo.getFont().deriveFont(Font.BOLD, 44f));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(logo);

        JLabel tagline = new JLabel("AI-powered test impact analysis & coverage for Java");
        tagline.setForeground(new Color(0xBFD4F5));
        tagline.setFont(tagline.getFont().deriveFont(Font.PLAIN, 16f));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(tagline);

        content.add(Box.createVerticalStrut(26));

        for (String feature : FEATURES) {
            JLabel f = new JLabel("✓   " + feature);
            f.setForeground(new Color(0xE6EEFB));
            f.setFont(f.getFont().deriveFont(Font.PLAIN, 14.5f));
            f.setAlignmentX(Component.CENTER_ALIGNMENT);
            f.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
            content.add(f);
        }

        content.add(Box.createVerticalStrut(30));

        JButton start = new JButton("Start Application  →");
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        start.setFocusPainted(false);
        start.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        start.setFont(start.getFont().deriveFont(Font.BOLD, 16f));
        start.setForeground(Color.WHITE);
        start.setBackground(new Color(0x2D7FF9));
        start.setBorder(BorderFactory.createEmptyBorder(12, 34, 12, 34));
        start.setMaximumSize(new Dimension(260, 50));
        start.putClientProperty("JButton.buttonType", "roundRect");
        start.addActionListener(e -> onStart.accept(this));
        content.add(start);

        JLabel hint = new JLabel("Configure your OpenAI key in Settings to enable AI features");
        hint.setForeground(new Color(0x8FA6CC));
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 11.5f));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        content.add(hint);

        return content;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) background.start();
        else background.stop();
    }
}
