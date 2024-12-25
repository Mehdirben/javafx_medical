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
        
        // Chat input handler
        chatInput.getStyleClass().add("chat-input");
        setupKeyHandler(chatInput, this::sendMessage);
        
        // Diagnosis input handler
        symptomsInput.getStyleClass().add("chat-input");
        setupKeyHandler(symptomsInput, this::getDiagnosis);
    }
    
    private void setupKeyHandler(TextArea textArea, Runnable action) {
        textArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                
                if (event.isShiftDown()) {
                    // Manually add new line at current caret position
                    int caretPosition = textArea.getCaretPosition();
                    String text = textArea.getText();
                    textArea.setText(text.substring(0, caretPosition) + "\n" + text.substring(caretPosition));
                    textArea.positionCaret(caretPosition + 1);
                } else {
                    action.run();
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
