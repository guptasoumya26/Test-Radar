package com.testradar.ui;

import com.testradar.AppContext;
import com.testradar.model.AppSettings;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

public class SettingsPanel extends JPanel {

    private final AppContext ctx;

    private final JPasswordField apiKeyField = new JPasswordField(30);
    private final JTextField modelField = new JTextField(20);
    private final JTextField baseUrlField = new JTextField(28);
    private final JComboBox<String> themeBox = new JComboBox<>(ThemeManager.themeNames());

    private final JTextField jiraUrlField = new JTextField(28);
    private final JTextField jiraEmailField = new JTextField(24);
    private final JPasswordField jiraTokenField = new JPasswordField(30);

    public SettingsPanel(AppContext ctx) {
        this.ctx = ctx;
        setBorder(UiUtils.pad(20));
        setLayout(new BorderLayout(0, 14));
        add(buildHeader(), BorderLayout.NORTH);
        add(new JScrollPane(buildForm()), BorderLayout.CENTER);
        loadFromSettings();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.add(UiUtils.title("Settings"), BorderLayout.NORTH);
        header.add(UiUtils.subtitle("Stored locally at " + ctx.getSettingsService().configFile()), BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));

        form.add(openAiCard());
        form.add(javax.swing.Box.createVerticalStrut(14));
        form.add(jiraCard());
        form.add(javax.swing.Box.createVerticalStrut(14));
        form.add(appearanceCard());
        form.add(javax.swing.Box.createVerticalStrut(14));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton save = new JButton("Save settings");
        UiUtils.primary(save);
        save.addActionListener(e -> save());
        actions.add(save);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        form.add(actions);
        return form;
    }

    private JPanel openAiCard() {
        JPanel card = UiUtils.card();
        card.setLayout(new GridBagLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        GridBagConstraints g = grid();

        addHeader(card, g, "OpenAI");
        addRow(card, g, "API key:", apiKeyField);
        addRow(card, g, "Model:", modelField);
        addRow(card, g, "Base URL:", baseUrlField);
        return card;
    }

    private JPanel jiraCard() {
        JPanel card = UiUtils.card();
        card.setLayout(new GridBagLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints g = grid();

        addHeader(card, g, "Jira (optional)");
        addRow(card, g, "Base URL:", jiraUrlField);
        addRow(card, g, "Email:", jiraEmailField);
        addRow(card, g, "API token:", jiraTokenField);
        return card;
    }

    private JPanel appearanceCard() {
        JPanel card = UiUtils.card();
        card.setLayout(new GridBagLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints g = grid();

        addHeader(card, g, "Appearance");
        addRow(card, g, "Theme:", themeBox);
        return card;
    }

    private GridBagConstraints grid() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        g.gridy = 0;
        return g;
    }

    private void addHeader(JPanel card, GridBagConstraints g, String text) {
        g.gridx = 0; g.gridwidth = 2; g.fill = GridBagConstraints.NONE;
        card.add(UiUtils.sectionHeader(text), g);
        g.gridy++; g.gridwidth = 1;
    }

    private void addRow(JPanel card, GridBagConstraints g, String label, Component field) {
        g.gridx = 0; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        card.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        card.add(field, g);
        g.gridy++;
    }

    private void loadFromSettings() {
        AppSettings s = ctx.getSettings();
        apiKeyField.setText(s.getOpenAiApiKey());
        modelField.setText(s.getOpenAiModel());
        baseUrlField.setText(s.getOpenAiBaseUrl());
        themeBox.setSelectedItem(s.getTheme());
        jiraUrlField.setText(s.getJiraBaseUrl());
        jiraEmailField.setText(s.getJiraEmail());
        jiraTokenField.setText(s.getJiraApiToken());
    }

    private void save() {
        AppSettings s = ctx.getSettings();
        s.setOpenAiApiKey(new String(apiKeyField.getPassword()).trim());
        s.setOpenAiModel(modelField.getText().trim());
        s.setOpenAiBaseUrl(baseUrlField.getText().trim());
        s.setJiraBaseUrl(jiraUrlField.getText().trim());
        s.setJiraEmail(jiraEmailField.getText().trim());
        s.setJiraApiToken(new String(jiraTokenField.getPassword()).trim());

        String selectedTheme = (String) themeBox.getSelectedItem();
        boolean themeChanged = selectedTheme != null && !selectedTheme.equals(s.getTheme());
        s.setTheme(selectedTheme);

        try {
            ctx.getSettingsService().save(s);
            ctx.rebuildClients();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not save: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (themeChanged) {
            ThemeManager.apply(selectedTheme);
            for (Window w : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(w);
            }
        }
        JOptionPane.showMessageDialog(this, "Settings saved.");
    }
}
