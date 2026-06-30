package com.testradar.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatNordIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ThemeManager {

    private static final Map<String, Class<? extends LookAndFeel>> THEMES = new LinkedHashMap<>();

    static {
        THEMES.put("FlatLaf Light", FlatLightLaf.class);
        THEMES.put("FlatLaf Dark", FlatDarkLaf.class);
        THEMES.put("FlatLaf IntelliJ", FlatIntelliJLaf.class);
        THEMES.put("FlatLaf Darcula", FlatDarculaLaf.class);
        THEMES.put("One Dark", FlatOneDarkIJTheme.class);
        THEMES.put("Arc Dark Orange", FlatArcDarkOrangeIJTheme.class);
        THEMES.put("Nord", FlatNordIJTheme.class);
    }

    private ThemeManager() {}

    public static String[] themeNames() {
        return THEMES.keySet().toArray(new String[0]);
    }

    public static void apply(String name) {
        Class<? extends LookAndFeel> laf = THEMES.getOrDefault(name, FlatLightLaf.class);
        try {
            UIManager.setLookAndFeel(laf.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ignored) {}
        }
    }
}
