package com.medical.service;

import okhttp3.*;
import java.io.IOException;
import java.util.Properties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.TimeUnit;

public class DiagnosisService {
    private final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    
    public DiagnosisService() {
        Properties props = new Properties();
        try {
            props.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
            this.apiKey = props.getProperty("huggingface.api.key");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load API key from properties", e);
        }
    }
    
    private static final String HUGGING_FACE_API = "https://api-inference.huggingface.co/models/mistralai/Mixtral-8x7B-Instruct-v0.1";
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    public String diagnose(String symptoms) throws IOException {
        IOException lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return attemptDiagnosis(symptoms);
            } catch (IOException e) {
                lastException = e;
                System.err.println("Attempt " + attempt + " failed: " + e.getMessage());
                
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry delay", ie);
                    }
                }
            }
        }
        
        throw new IOException("Failed after " + MAX_RETRIES + " attempts. Last error: " + 
            (lastException != null ? lastException.getMessage() : "Unknown error"));
    }

    private String attemptDiagnosis(String symptoms) throws IOException {
        ObjectNode jsonNode = objectMapper.createObjectNode();
        String prompt = "[INST] You are a medical AI assistant. Given these symptoms, provide a detailed medical diagnosis and always include a disclaimer about consulting healthcare professionals. Symptoms: " + symptoms + " [/INST]";
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
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                throw new IOException("API request failed with code: " + response.code() 
                    + ", message: " + response.message() 
                    + ", body: " + errorBody);
            }
            
            String responseBody = response.body().string();
            System.out.println("API Response: " + responseBody);
            
            try {
                String generatedText = objectMapper.readTree(responseBody).get(0).get("generated_text").asText();
                // Clean up the response
                return cleanResponse(generatedText, symptoms);
            } catch (Exception e) {
                System.err.println("Error parsing response: " + responseBody);
                throw new IOException("Failed to parse API response: " + responseBody, e);
            }
        }
    }

    private String cleanResponse(String response, String symptoms) {
        // Remove tags and clean up text
        String cleaned = response
            .replaceAll("(?i)^Error:\\s*", "")       // Remove "Error:" prefix at the start (case insensitive)
            .replaceAll("\\[INST\\].*?Symptoms:", "") // Remove everything up to Symptoms:
            .replaceAll("\\[/INST\\]", "")           // Remove closing instruction tag
            .replaceAll("<s>|</s>", "")              // Remove XML-like tags
            .replaceAll("^[^a-zA-Z]*", "")           // Remove any non-letter characters from start
            .replaceAll(symptoms + "\\s*", "")        // Remove the symptoms text
            .trim();                                  // Remove extra whitespace

        // Format the text with proper line breaks and sections
        String[] paragraphs = cleaned.split("\\*\\*");
        StringBuilder formatted = new StringBuilder();

        for (String paragraph : paragraphs) {
            if (paragraph.trim().toLowerCase().startsWith("disclaimer")) {
                formatted.append("\n\n--- Disclaimer ---\n")
                        .append(paragraph.trim().replace("Disclaimer:", ""));
            } else {
                formatted.append("\n").append(paragraph.trim());
            }
        }

        // Add some spacing and structure
        return formatted.toString()
            .replaceAll("\\s{2,}", "\n")  // Replace multiple spaces with newline
            .replaceAll("\\. ", ".\n")    // Add line break after each sentence
            .trim();
    }
}
