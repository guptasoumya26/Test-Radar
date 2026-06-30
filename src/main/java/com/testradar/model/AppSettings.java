package com.testradar.model;

public class AppSettings {

    private String openAiApiKey = "";
    private String openAiModel = "gpt-4o-mini";
    private String openAiBaseUrl = "https://api.openai.com/v1";
    private String theme = "FlatLaf Light";

    private String jiraBaseUrl = "";
    private String jiraEmail = "";
    private String jiraApiToken = "";

    public String getOpenAiApiKey() {
        return openAiApiKey == null ? "" : openAiApiKey;
    }

    public void setOpenAiApiKey(String openAiApiKey) {
        this.openAiApiKey = openAiApiKey;
    }

    public String getOpenAiModel() {
        return (openAiModel == null || openAiModel.isBlank()) ? "gpt-4o-mini" : openAiModel;
    }

    public void setOpenAiModel(String openAiModel) {
        this.openAiModel = openAiModel;
    }

    public String getOpenAiBaseUrl() {
        return (openAiBaseUrl == null || openAiBaseUrl.isBlank())
                ? "https://api.openai.com/v1" : openAiBaseUrl.replaceAll("/+$", "");
    }

    public void setOpenAiBaseUrl(String openAiBaseUrl) {
        this.openAiBaseUrl = openAiBaseUrl;
    }

    public String getTheme() {
        return theme == null ? "FlatLaf Light" : theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public boolean hasApiKey() {
        return openAiApiKey != null && !openAiApiKey.isBlank();
    }

    public String getJiraBaseUrl() {
        return jiraBaseUrl == null ? "" : jiraBaseUrl.replaceAll("/+$", "");
    }

    public void setJiraBaseUrl(String jiraBaseUrl) { this.jiraBaseUrl = jiraBaseUrl; }

    public String getJiraEmail() { return jiraEmail == null ? "" : jiraEmail; }
    public void setJiraEmail(String jiraEmail) { this.jiraEmail = jiraEmail; }

    public String getJiraApiToken() { return jiraApiToken == null ? "" : jiraApiToken; }
    public void setJiraApiToken(String jiraApiToken) { this.jiraApiToken = jiraApiToken; }

    public boolean hasJira() {
        return !getJiraBaseUrl().isBlank() && !getJiraEmail().isBlank() && !getJiraApiToken().isBlank();
    }
}
