package com.testradar.ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

public final class UiUtils {

    public static final Color ACCENT = new Color(0x2D7FF9);

    private UiUtils() {}

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 20f));
        return l;
    }

    public static JLabel subtitle(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(0x6B7280));
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 12.5f));
        return l;
    }

    public static JLabel sectionHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
        return l;
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.putClientProperty("FlatLaf.style", "arc: 16; background: lighten($Panel.background,2%)");
        p.setBorder(pad(16));
        return p;
    }

    public static Border pad(int all) {
        return BorderFactory.createEmptyBorder(all, all, all, all);
    }

    public static Border pad(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    public static void primary(JComponent c) {
        c.putClientProperty("JButton.buttonType", "default");
    }

    public static void leftAlign(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}
