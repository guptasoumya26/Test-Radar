package com.testradar;

import com.formdev.flatlaf.FlatLaf;
import com.testradar.ui.MainFrame;
import com.testradar.ui.SplashScreen;
import com.testradar.ui.ThemeManager;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        System.setProperty("flatlaf.useWindowDecorations", "true");

        SwingUtilities.invokeLater(() -> {
            AppContext ctx = new AppContext();

            ThemeManager.apply("FlatLaf Dark");
            tuneGlobals();

            SplashScreen splash = new SplashScreen(s -> {
                s.setVisible(false);
                s.dispose();
                ThemeManager.apply(ctx.getSettings().getTheme());
                tuneGlobals();
                MainFrame frame = new MainFrame(ctx);
                FlatLaf.updateUI();
                frame.setVisible(true);
            });
            splash.setVisible(true);
        });
    }

    private static void tuneGlobals() {
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 10);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        UIManager.put("TabbedPane.tabHeight", 38);
        UIManager.put("TabbedPane.showTabSeparators", true);
    }
}
