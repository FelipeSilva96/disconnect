package com.disconnect.service.ia;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.disconnect.util.AppConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ClaudeApiService {

    private static final String ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final HttpClient httpClient;
    private final Gson gson;
    private final String apiKey;
    private final String model;

    public ClaudeApiService(Gson gson) {
        this.gson = gson;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        this.apiKey = AppConfig.get("anthropic.api.key", "");
        this.model = AppConfig.get("anthropic.model", "claude-haiku-4-5-20251001");
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String enviarMensagem(String systemPrompt, String userPrompt, int maxTokens) {
        if (!isConfigured()) {
            throw new IllegalStateException("ANTHROPIC_API_KEY nao configurada no ambiente do servidor.");
        }

        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "system", systemPrompt,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", userPrompt)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ANTHROPIC_URL))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", ANTHROPIC_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException(
                        "Erro na API da Anthropic: HTTP " + response.statusCode() + " - " + response.body());
            }

            return extrairTexto(response.body());
        } catch (IOException e) {
            throw new RuntimeException("Erro de rede ao chamar a Anthropic: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Chamada a Anthropic interrompida.", e);
        }
    }

    private String extrairTexto(String responseBody) {
        JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray content = root.getAsJsonArray("content");

        StringBuilder sb = new StringBuilder();

        if (content != null) {
            for (JsonElement item : content) {
                JsonObject block = item.getAsJsonObject();
                String type = block.has("type") ? block.get("type").getAsString() : "";

                if ("text".equals(type) && block.has("text")) {
                    sb.append(block.get("text").getAsString());
                }
            }
        }

        return sb.toString().trim();
    }
}