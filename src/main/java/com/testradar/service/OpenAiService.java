package com.testradar.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.testradar.model.AppSettings;
import com.testradar.model.TestCase;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class OpenAiService {

    public record Message(String role, String content) {}

    private final AppSettings settings;
    private final HttpClient http;
    private final Gson gson = new Gson();

    public OpenAiService(AppSettings settings) {
        this.settings = settings;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String chat(List<Message> messages) throws Exception {
        return complete(messages, false);
    }

    private String complete(List<Message> messages, boolean jsonMode) throws Exception {
        if (!settings.hasApiKey()) {
            throw new IllegalStateException("No OpenAI API key configured. Add it in the Settings tab.");
        }

        JsonObject body = new JsonObject();
        body.addProperty("model", settings.getOpenAiModel());
        body.addProperty("temperature", 0.2);
        if (jsonMode) {
            JsonObject fmt = new JsonObject();
            fmt.addProperty("type", "json_object");
            body.add("response_format", fmt);
        }
        JsonArray msgs = new JsonArray();
        for (Message m : messages) {
            JsonObject o = new JsonObject();
            o.addProperty("role", m.role());
            o.addProperty("content", m.content());
            msgs.add(o);
        }
        body.add("messages", msgs);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(settings.getOpenAiBaseUrl() + "/chat/completions"))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + settings.getOpenAiApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new RuntimeException("OpenAI API error " + response.statusCode()
                    + ": " + extractError(response.body()));
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return json.getAsJsonArray("choices").get(0).getAsJsonObject()
                .getAsJsonObject("message").get("content").getAsString().trim();
    }

    public String analyzeRelevance(String changeDescription, List<TestCase> testCases,
                                   String suiteContext) throws Exception {
        StringBuilder catalog = new StringBuilder();
        for (int i = 0; i < testCases.size(); i++) {
            TestCase tc = testCases.get(i);
            catalog.append(i).append(": ")
                   .append(tc.getFullyQualifiedClassName()).append('#').append(tc.getMethodName());
            if (!tc.getDescription().isBlank()) catalog.append(" - ").append(tc.getDescription());
            if (!tc.getGroups().isEmpty()) catalog.append(" [groups: ").append(String.join(",", tc.getGroups())).append(']');
            catalog.append('\n');
        }

        String system = """
                You are a senior test engineer performing change-impact analysis on a Java test suite.
                Given a description of an upcoming code change and a catalog of existing test cases,
                identify which tests are most relevant to run to validate the change and guard against regressions.
                Respond ONLY with a JSON object of this exact shape:
                {
                  "summary": "<2-3 sentence overview of the impact and testing strategy>",
                  "results": [ { "id": <int index from catalog>, "score": <0.0-1.0 relevance>, "reason": "<why this test matters for the change>" } ]
                }
                Only include tests with score >= 0.4. Order results by descending score.
                """;

        String user = "UPCOMING CHANGE:\n" + changeDescription
                + "\n\nSUITE CONTEXT:\n" + truncate(suiteContext, 4000)
                + "\n\nTEST CASE CATALOG (index: id):\n" + catalog;

        String content = complete(List.of(
                new Message("system", system),
                new Message("user", user)), true);

        return applyRelevance(content, testCases);
    }

    private String applyRelevance(String content, List<TestCase> testCases) {
        for (TestCase tc : testCases) {
            tc.setRelevanceScore(0);
            tc.setRelevanceReason("");
            tc.setSelected(false);
        }
        String json = stripFences(content);
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        String summary = obj.has("summary") ? obj.get("summary").getAsString() : "";
        if (obj.has("results")) {
            for (var el : obj.getAsJsonArray("results")) {
                JsonObject r = el.getAsJsonObject();
                int id = r.get("id").getAsInt();
                if (id < 0 || id >= testCases.size()) continue;
                TestCase tc = testCases.get(id);
                tc.setRelevanceScore(r.has("score") ? r.get("score").getAsDouble() : 0);
                tc.setRelevanceReason(r.has("reason") ? r.get("reason").getAsString() : "");
                tc.setSelected(tc.getRelevanceScore() >= 0.6);
            }
        }
        return summary;
    }

    private String extractError(String body) {
        try {
            JsonObject o = JsonParser.parseString(body).getAsJsonObject();
            if (o.has("error")) {
                return o.getAsJsonObject("error").get("message").getAsString();
            }
        } catch (Exception ignored) {}
        return body == null ? "unknown" : body;
    }

    private String stripFences(String s) {
        s = s.trim();
        if (s.startsWith("```")) {
            int firstNl = s.indexOf('\n');
            if (firstNl > 0) s = s.substring(firstNl + 1);
            if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        }
        return s.trim();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "\n...[truncated]";
    }

    public List<Message> emptyHistory() {
        return new ArrayList<>();
    }
}
