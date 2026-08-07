package com.mizomixstation.app;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends AppCompatActivity {
    private final ExecutorService aiExecutor = Executors.newSingleThreadExecutor();

    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;
    private GeminiAiService aiService;

    private EditText promptInput;
    private Spinner languageSpinner;
    private TextView responseText;
    private Button sendButton;
    private Button speakButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        promptInput = findViewById(R.id.promptInput);
        languageSpinner = findViewById(R.id.languageSpinner);
        responseText = findViewById(R.id.responseText);
        sendButton = findViewById(R.id.sendButton);
        speakButton = findViewById(R.id.speakButton);
        progressBar = findViewById(R.id.progressBar);

        String[] languages = {"Mizo", "English", "Burmese", "Hindi"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                languages
        );
        languageSpinner.setAdapter(adapter);

        initializeTextToSpeech();

        try {
            aiService = new GeminiAiService(aiExecutor);
        } catch (RuntimeException error) {
            responseText.setText("Firebase initialization failed: " + error.getMessage());
            sendButton.setEnabled(false);
        }

        sendButton.setOnClickListener(view -> sendPrompt());
        speakButton.setOnClickListener(view -> speakResponse());
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS && textToSpeech != null) {
                int result = textToSpeech.setLanguage(Locale.US);
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED;
                speakButton.setEnabled(ttsReady);
            } else {
                ttsReady = false;
                speakButton.setEnabled(false);
            }
        });
    }

    private void sendPrompt() {
        if (aiService == null) {
            Toast.makeText(this, "AI service is not ready.", Toast.LENGTH_SHORT).show();
            return;
        }

        String prompt = promptInput.getText().toString().trim();
        if (prompt.isEmpty()) {
            promptInput.setError("Please write a message.");
            return;
        }

        String language = String.valueOf(languageSpinner.getSelectedItem());
        setLoading(true);
        responseText.setText("Thinking...");

        aiService.ask(prompt, language, new GeminiAiService.Callback() {
            @Override
            public void onSuccess(String text) {
                runOnUiThread(() -> {
                    responseText.setText(text);
                    setLoading(false);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    responseText.setText("Error: " + message);
                    setLoading(false);
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        sendButton.setEnabled(!loading && aiService != null);
    }

    private void speakResponse() {
        if (!ttsReady || textToSpeech == null) {
            Toast.makeText(this, "Text-to-Speech is not ready.", Toast.LENGTH_SHORT).show();
            return;
        }
        String text = responseText.getText().toString();
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mizo_mixstation_ai_response");
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        aiExecutor.shutdownNow();
        super.onDestroy();
    }
}
