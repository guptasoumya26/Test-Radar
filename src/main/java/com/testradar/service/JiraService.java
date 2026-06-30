package com.testradar.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.testradar.model.AppSettings;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

public class JiraService {

    public record JiraIssue(String key, String summary, String description, String issueType, String status) {
        public String asChangeText() {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(key).append("] ").append(summary).append("\n");
            if (issueType != null && !issueType.isBlank()) sb.append("Type: ").append(issueType).append("\n");
            if (status != null && !status.isBlank()) sb.append("Status: ").append(status).append("\n");
            if (description != null && !description.isBlank()) {
                sb.append("\n").append(description);
            }
            return sb.toString().trim();
        }
    }

    private final AppSettings settings;
    private final HttpClient http;

    public JiraService(AppSettings settings) {
        this.settings = settings;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    }

    public JiraIssue fetchIssue(String issueKey) throws Exception {
        if (!settings.hasJira()) {
            throw new IllegalStateException("Jira is not configured. Add base URL, email and API token in Settings.");
        }
        String key = issueKey.trim();
        String url = settings.getJiraBaseUrl() + "/rest/api/2/issue/" + key
                + "?fields=summary,description,issuetype,status";

        String creds = settings.getJiraEmail() + ":" + settings.getJiraApiToken();
        String auth = Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Basic " + auth)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new RuntimeException("Jira API error " + response.statusCode()
                    + " for issue " + key + ": " + response.body());
        }

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject fields = root.getAsJsonObject("fields");
        String summary = textOrEmpty(fields.get("summary"));
        String description = textOrEmpty(fields.get("description"));
        String type = fields.has("issuetype") && !fields.get("issuetype").isJsonNull()
                ? textOrEmpty(fields.getAsJsonObject("issuetype").get("name")) : "";
        String status = fields.has("status") && !fields.get("status").isJsonNull()
                ? textOrEmpty(fields.getAsJsonObject("status").get("name")) : "";

        return new JiraIssue(key, summary, description, type, status);
    }

    private String textOrEmpty(JsonElement el) {
        if (el == null || el.isJsonNull()) return "";
        return el.isJsonPrimitive() ? el.getAsString() : el.toString();
    }
}
