package com.testradar.ui;

import com.testradar.AppContext;
import com.testradar.model.TestCase;
import com.testradar.service.OpenAiService.Message;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

public class ChatPanel extends JPanel {

    private final AppContext ctx;
    private final JTextArea transcript = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JButton sendButton = new JButton("Send");
    private final JButton clearButton = new JButton("Clear");
    private final List<Message> history = new ArrayList<>();

    public ChatPanel(AppContext ctx) {
        this.ctx = ctx;
        setBorder(UiUtils.pad(20));
        setLayout(new BorderLayout(0, 12));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        wire();
        resetTranscript();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.add(UiUtils.title("Ask the Suite"), BorderLayout.NORTH);
        header.add(UiUtils.subtitle("Chat with AI about the test suite - coverage gaps, what a test does, where to add tests, etc."),
                BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 10));
        transcript.setEditable(false);
        transcript.setLineWrap(true);
        transcript.setWrapStyleWord(true);
        transcript.setBorder(UiUtils.pad(10));
        body.add(new JScrollPane(transcript), BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputField.putClientProperty("JTextField.placeholderText", "Ask about the test suite…");
        inputRow.add(inputField, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        UiUtils.primary(sendButton);
        buttons.add(sendButton);
        buttons.add(clearButton);
        inputRow.add(buttons, BorderLayout.EAST);
        body.add(inputRow, BorderLayout.SOUTH);
        return body;
    }

    private void wire() {
        sendButton.addActionListener(e -> send());
        inputField.addActionListener(e -> send());
        clearButton.addActionListener(e -> { history.clear(); resetTranscript(); });
    }

    private void resetTranscript() {
        transcript.setText("Test Radar AI is ready. Ask anything about the loaded test suite.\n\n");
    }

    private String buildSuiteContext() {
        StringBuilder sb = new StringBuilder();
        if (ctx.getRepoInfo() != null) {
            sb.append("Repository: ").append(ctx.getRepoInfo().getSource()).append('\n');
        }
        if (ctx.getCoverageReport() != null) {
            var r = ctx.getCoverageReport();
            sb.append(String.format("Coverage: %.1f%% (%d/%d classes), %d test classes, %d tests.%n",
                    r.getCoveragePercent(), r.getCoveredSourceClasses(), r.getTotalSourceClasses(),
                    r.getTotalTestClasses(), r.getTotalTests()));
        }
        sb.append("\nTest catalog:\n");
        int limit = 0;
        for (TestCase tc : ctx.getTestCases()) {
            sb.append("- ").append(tc.getFullyQualifiedClassName()).append('#').append(tc.getMethodName());
            if (!tc.getDescription().isBlank()) sb.append(" - ").append(tc.getDescription());
            sb.append('\n');
            if (++limit >= 400) { sb.append("...[truncated]\n"); break; }
        }
        return sb.toString();
    }

    private void send() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        if (!ctx.getSettings().hasApiKey()) {
            JOptionPane.showMessageDialog(this, "Add your OpenAI API key in the Settings tab first.");
            return;
        }
        if (!ctx.hasSuite()) {
            JOptionPane.showMessageDialog(this, "Scan and analyse a repository first (Scan tab).");
            return;
        }
        inputField.setText("");
        append("You", text);
        history.add(new Message("user", text));
        sendButton.setEnabled(false);

        final List<Message> request = new ArrayList<>();
        request.add(new Message("system",
                "You are Test Radar's assistant, an expert Java test engineer. Answer questions about the "
                + "user's test suite using the context below. Be concise and practical.\n\n"
                + "SUITE CONTEXT:\n" + buildSuiteContext()));
        request.addAll(history);

        new SwingWorker<String, Void>() {
            Exception error;
            @Override protected String doInBackground() {
                try { return ctx.getOpenAiService().chat(request); }
                catch (Exception ex) { error = ex; return null; }
            }
            @Override protected void done() {
                sendButton.setEnabled(true);
                if (error != null) {
                    append("Error", error.getMessage());
                    return;
                }
                String reply = "";
                try { reply = get(); } catch (Exception ignored) {}
                history.add(new Message("assistant", reply));
                append("Test Radar AI", reply);
            }
        }.execute();
    }

    private void append(String who, String message) {
        transcript.append(who + ":\n" + message + "\n\n");
        transcript.setCaretPosition(transcript.getDocument().getLength());
    }
}
