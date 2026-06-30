package com.testradar.ui.components;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NeuralNetworkPanel extends JPanel {

    private static final int NODE_COUNT = 60;
    private static final double LINK_DISTANCE = 170;

    private static class Node {
        double x, y, vx, vy, pulse;
    }

    private final List<Node> nodes = new ArrayList<>();
    private final Random rnd = new Random();
    private final Timer timer;
    private double phase = 0;

    public NeuralNetworkPanel() {
        setOpaque(true);
        timer = new Timer(33, e -> tick());
    }

    public void start() {
        if (nodes.isEmpty()) initNodes();
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    private void initNodes() {
        int w = Math.max(getWidth(), 800);
        int h = Math.max(getHeight(), 560);
        for (int i = 0; i < NODE_COUNT; i++) {
            Node n = new Node();
            n.x = rnd.nextInt(w);
            n.y = rnd.nextInt(h);
            n.vx = (rnd.nextDouble() - 0.5) * 0.7;
            n.vy = (rnd.nextDouble() - 0.5) * 0.7;
            n.pulse = rnd.nextDouble() * Math.PI * 2;
            nodes.add(n);
        }
    }

    private void tick() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;
        if (nodes.isEmpty()) initNodes();
        phase += 0.02;
        for (Node n : nodes) {
            n.x += n.vx;
            n.y += n.vy;
            n.pulse += 0.05;
            if (n.x < 0 || n.x > w) n.vx = -n.vx;
            if (n.y < 0 || n.y > h) n.vy = -n.vy;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setPaint(new GradientPaint(0, 0, new Color(0x0A1026), 0, h, new Color(0x0E1E3A)));
        g2.fillRect(0, 0, w, h);

        drawWatermark(g2, w, h);

        if (nodes.isEmpty()) { g2.dispose(); return; }

        for (int i = 0; i < nodes.size(); i++) {
            Node a = nodes.get(i);
            for (int j = i + 1; j < nodes.size(); j++) {
                Node b = nodes.get(j);
                double dx = a.x - b.x, dy = a.y - b.y;
                double dist = Math.hypot(dx, dy);
                if (dist < LINK_DISTANCE) {
                    float alpha = (float) (1.0 - dist / LINK_DISTANCE) * 0.55f;
                    g2.setColor(new Color(0.28f, 0.55f, 0.98f, alpha));
                    g2.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
                }
            }
        }

        for (Node n : nodes) {
            double pulse = 2.5 + 1.8 * (0.5 + 0.5 * Math.sin(n.pulse));
            int r = (int) (pulse);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
            g2.setColor(new Color(0x4DA3FF));
            g2.fillOval((int) (n.x - r * 3), (int) (n.y - r * 3), r * 6, r * 6);
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setColor(new Color(0x8FC3FF));
            g2.fillOval((int) (n.x - r), (int) (n.y - r), r * 2, r * 2);
        }

        g2.dispose();
    }

    private void drawWatermark(Graphics2D g2, int w, int h) {
        String text = "AI";
        Font font = getFont().deriveFont(Font.BOLD, Math.min(w, h) * 0.42f);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        int tx = (w - tw) / 2;
        int ty = h / 2 + fm.getAscent() / 2 - fm.getDescent();
        float glow = (float) (0.10 + 0.05 * Math.sin(phase));
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glow));
        g2.setColor(new Color(0x2D7FF9));
        g2.drawString(text, tx, ty);
        g2.setComposite(AlphaComposite.SrcOver);
    }
}
