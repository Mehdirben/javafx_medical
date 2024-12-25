package com.medical.service;

import okhttp3.*;
import java.io.IOException;
import java.util.Properties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ChatbotService {
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    
    public ChatbotService() {
        Properties props = new Properties();
        try {
            props.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
            this.apiKey = props.getProperty("huggingface.api.key");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load API key from properties", e);
        }
    }
    
    private static final String HUGGING_FACE_API = "https://api-inference.huggingface.co/models/mistralai/Mixtral-8x7B-Instruct-v0.1";

    public String processQuery(String query) throws IOException {
        ObjectNode jsonNode = objectMapper.createObjectNode();
        String prompt = "<s>[INST] You are a medical AI assistant. Always provide accurate medical information and include a reminder to consult healthcare professionals. Question: " + query + " [/INST]</s>";
        jsonNode.put("inputs", prompt);
        
        RequestBody body = RequestBody.create(
            jsonNode.toString(),
            MediaType.parse("application/json")
        );
        
        Request request = new Request.Builder()
                .url(HUGGING_FACE_API)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response " + response);
            }
            String responseBody = response.body().string();
            String generatedText = objectMapper.readTree(responseBody).get(0).get("generated_text").asText();
            return cleanResponse(generatedText, query);
        }
    }

    private String cleanResponse(String response, String currentQuery) {
        return response
            .replaceAll("(?i)^error:\\s*", "")       // Remove "Error:" prefix at the start (case insensitive)
            .replaceAll("\\[INST\\].*?Question:", "") // Remove everything up to Question:
            .replaceAll("\\[/INST\\]", "")           // Remove closing instruction tag
            .replaceAll("<s>|</s>", "")              // Remove XML-like tags
            .replaceAll("^[^a-zA-Z]*", "")           // Remove any non-letter characters from start
            .replaceAll("<[^>]*>", "")               // Remove any leftover HTML tags
            .replaceAll(" c style=\".*?\">", "")  // Remove leftover ' c style="..."' snippet
            .replaceAll("(?i)" + java.util.regex.Pattern.quote(currentQuery), "") // Remove the user's input
            .trim();
    }
}
