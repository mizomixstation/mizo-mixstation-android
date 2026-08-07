package com.mizomixstation.app;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeBackend;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeModel;

import java.util.concurrent.Executor;

public final class GeminiAiService {
    public interface Callback {
        void onSuccess(String text);
        void onError(String message);
    }

    private final GenerativeModelFutures model;
    private final Executor executor;

    public GeminiAiService(@NonNull Executor executor) {
        this.executor = executor;
        GenerativeModel raw = FirebaseAI
                .getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-3.6-flash");
        this.model = GenerativeModelFutures.from(raw);
    }

    public void ask(String userPrompt, String language, @NonNull Callback callback) {
        String safePrompt = userPrompt == null ? "" : userPrompt.trim();
        String safeLanguage = language == null || language.trim().isEmpty()
                ? "Mizo"
                : language.trim();

        String systemPrompt =
                "You are Mizo MixStation AI Assistant. "
                        + "Support Mizo, English, Burmese and Hindi. "
                        + "Use the user's language. Keep answers clear, respectful and practical. "
                        + "Language preference: " + safeLanguage
                        + "\n\nUser: " + safePrompt;

        Content content = new Content.Builder()
                .addText(systemPrompt)
                .build();

        ListenableFuture<GenerateContentResponse> future = model.generateContent(content);
        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String text = result == null ? null : result.getText();
                if (text == null || text.trim().isEmpty()) {
                    callback.onError("AI response was empty.");
                    return;
                }
                callback.onSuccess(text.trim());
            }

            @Override
            public void onFailure(@NonNull Throwable throwable) {
                String message = throwable.getMessage();
                callback.onError(message == null || message.trim().isEmpty()
                        ? "AI request failed. Check Firebase AI Logic and App Check setup."
                        : message);
            }
        }, executor);
    }
}
