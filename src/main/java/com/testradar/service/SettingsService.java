package com.testradar.service;

import com.testradar.model.AppSettings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class SettingsService {

    public static final Path CONFIG_DIR = Path.of(System.getProperty("user.home"), ".testradar");
    public static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.properties");

    private static final String KEY_API = "openai.apiKey";
    private static final String KEY_MODEL = "openai.model";
    private static final String KEY_BASE_URL = "openai.baseUrl";
    private static final String KEY_THEME = "ui.theme";
    private static final String KEY_JIRA_URL = "jira.baseUrl";
    private static final String KEY_JIRA_EMAIL = "jira.email";
    private static final String KEY_JIRA_TOKEN = "jira.apiToken";

    public AppSettings load() {
        AppSettings settings = new AppSettings();
        if (!Files.exists(CONFIG_FILE)) {
            return settings;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(CONFIG_FILE)) {
            props.load(in);
            settings.setOpenAiApiKey(props.getProperty(KEY_API, "").trim());
            settings.setOpenAiModel(props.getProperty(KEY_MODEL, settings.getOpenAiModel()).trim());
            settings.setOpenAiBaseUrl(props.getProperty(KEY_BASE_URL, settings.getOpenAiBaseUrl()).trim());
            settings.setTheme(props.getProperty(KEY_THEME, settings.getTheme()).trim());
            settings.setJiraBaseUrl(props.getProperty(KEY_JIRA_URL, "").trim());
            settings.setJiraEmail(props.getProperty(KEY_JIRA_EMAIL, "").trim());
            settings.setJiraApiToken(props.getProperty(KEY_JIRA_TOKEN, "").trim());
        } catch (IOException e) {
            System.err.println("Could not read settings: " + e.getMessage());
        }
        return settings;
    }

    public void save(AppSettings settings) {
        Properties props = new Properties();
        props.setProperty(KEY_API, settings.getOpenAiApiKey());
        props.setProperty(KEY_MODEL, settings.getOpenAiModel());
        props.setProperty(KEY_BASE_URL, settings.getOpenAiBaseUrl());
        props.setProperty(KEY_THEME, settings.getTheme());
        props.setProperty(KEY_JIRA_URL, settings.getJiraBaseUrl());
        props.setProperty(KEY_JIRA_EMAIL, settings.getJiraEmail());
        props.setProperty(KEY_JIRA_TOKEN, settings.getJiraApiToken());
        try {
            Files.createDirectories(CONFIG_DIR);
            try (OutputStream out = Files.newOutputStream(CONFIG_FILE)) {
                props.store(out, "Test Radar configuration");
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not save settings: " + e.getMessage(), e);
        }
    }

    public Path configFile() {
        return CONFIG_FILE;
    }
}
