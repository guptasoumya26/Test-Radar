package com.testradar.ui.components;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;

public class DonutChart extends JComponent {

    private double percent = 0;
    private String caption = "";
    private Color arcColor = new Color(0x2D7FF9);

    public void setValue(double percent, String caption) {
        this.percent = Math.max(0, Math.min(100, percent));
        this.caption = caption == null ? "" : caption;
        repaint();
    }

    public void setArcColor(Color c) {
        this.arcColor = c;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(220, 220);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight()) - 20;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;
        float stroke = Math.max(14f, size * 0.12f);

        g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Color track = getForeground() == null ? new Color(0xE5E7EB)
                : new Color(getForeground().getRed(), getForeground().getGreen(), getForeground().getBlue(), 40);
        g2.setColor(track);
        g2.draw(new Arc2D.Double(x, y, size, size, 0, 360, Arc2D.OPEN));

        g2.setColor(arcColor);
        double angle = 360.0 * (percent / 100.0);
        g2.draw(new Arc2D.Double(x, y, size, size, 90, -angle, Arc2D.OPEN));

        String pctText = String.format("%.0f%%", percent);
        Font big = getFont().deriveFont(Font.BOLD, size * 0.22f);
        g2.setFont(big);
        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(pctText);
        g2.drawString(pctText, getWidth() / 2 - tw / 2, getHeight() / 2 + fm.getAscent() / 2 - 4);

        if (!caption.isEmpty()) {
            Font small = getFont().deriveFont(Font.PLAIN, size * 0.075f);
            g2.setFont(small);
            FontMetrics fm2 = g2.getFontMetrics();
            int cw = fm2.stringWidth(caption);
            g2.setColor(new Color(0x6B7280));
            g2.drawString(caption, getWidth() / 2 - cw / 2, getHeight() / 2 + fm.getAscent() / 2 + fm2.getHeight());
        }
        g2.dispose();
    }
}
