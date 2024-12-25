package com.medical.controller;

import com.medical.service.ChatbotService;
import com.medical.service.DiagnosisService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class MainController {
    @FXML private VBox chatPane;
    @FXML private VBox diagnosisPane;
    @FXML private TextArea chatMessages;
    @FXML private TextArea chatInput;  // Changed from TextField to TextArea
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
        
        chatInput.getStyleClass().add("chat-input");
        
        // Replace the existing key event handler with this one
        chatInput.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume(); // Prevent the enter from being processed
                
                if (event.isShiftDown()) {
                    // Manually add new line at current caret position
                    int caretPosition = chatInput.getCaretPosition();
                    String text = chatInput.getText();
                    chatInput.setText(text.substring(0, caretPosition) + "\n" + text.substring(caretPosition));
                    chatInput.positionCaret(caretPosition + 1);
                } else {
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
