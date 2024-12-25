package com.medical.controller;

import com.medical.service.ChatbotService;
import com.medical.service.DiagnosisService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class MainController {
    @FXML private VBox chatPane;
    @FXML private VBox diagnosisPane;
    @FXML private TextArea chatMessages;
    @FXML private TextField chatInput;
    @FXML private TextArea symptomsInput;
    @FXML private TextArea diagnosisResult;

    private final ChatbotService chatbotService;
    private final DiagnosisService diagnosisService;

    public MainController() {
        this.chatbotService = new ChatbotService();
        this.diagnosisService = new DiagnosisService();
    }

    @FXML
    public void initialize() {
        // Show chat pane by default
        showChat();
        
        // Add key event handler for chat input
        chatInput.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (event.isShiftDown()) {
                    // Shift+Enter: add new line
                    chatInput.appendText("\n");
                } else {
                    // Enter only: send message
                    event.consume(); // Prevent default Enter behavior
                    sendMessage();
                }
            }
        });
    }

    @FXML
    public void showChat() {
        chatPane.setVisible(true);
        diagnosisPane.setVisible(false);
    }

    @FXML
    public void showDiagnosis() {
        chatPane.setVisible(false);
        diagnosisPane.setVisible(true);
    }

    @FXML
    public void sendMessage() {
        String message = chatInput.getText().trim();
        if (message.isEmpty()) return;

        chatMessages.appendText("You: " + message + "\n");
        chatInput.clear();

        new Thread(() -> {
            try {
                String response = chatbotService.processQuery(message);
                Platform.runLater(() -> 
                    chatMessages.appendText("Bot: " + response + "\n\n"));
            } catch (Exception e) {
                Platform.runLater(() -> 
                    chatMessages.appendText("Error: " + e.getMessage() + "\n\n"));
            }
        }).start();
    }

    @FXML
    public void getDiagnosis() {
        String symptoms = symptomsInput.getText().trim();
        if (symptoms.isEmpty()) return;

        diagnosisResult.setText("Analyzing symptoms...");

        new Thread(() -> {
            try {
                String diagnosis = diagnosisService.diagnose(symptoms);
                Platform.runLater(() -> diagnosisResult.setText(diagnosis));
            } catch (Exception e) {
                Platform.runLater(() -> 
                    diagnosisResult.setText("Error: " + e.getMessage()));
            }
        }).start();
    }
}
