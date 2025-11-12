package com.aicareer.core.service.gigachat;

import chat.giga.client.*;
import chat.giga.client.auth.AuthClient;
import chat.giga.client.auth.AuthClientBuilder;
import chat.giga.model.ModelName;
import chat.giga.model.Scope;
import chat.giga.model.completion.ChatMessage;
import chat.giga.model.completion.ChatMessageRole;
import chat.giga.model.completion.CompletionRequest;
import chat.giga.model.completion.CompletionResponse;

public class GigaChatService {

    public String sendMessage(String prompt) {
        GigaChatClient client = GigaChatClient.builder()
                .verifySslCerts(false)
                .authClient(AuthClient.builder()
                        .withOAuth(AuthClientBuilder.OAuthBuilder.builder()
                                .scope(Scope.GIGACHAT_API_PERS)
                                .authKey("MDE5YTQwOGYtMmUwOS03MDQ5LThjNzUtYTk1OTgzZjljNzE2OmRkNWYxZGU3LWJjN2QtNDFjOS04MzE0LTRmYjNkYWYyZWQ0ZA==")
                                .build())
                        .build())
                .build();

        CompletionResponse response = client.completions(CompletionRequest.builder()
                .model(ModelName.GIGA_CHAT_2)
                .message(ChatMessage.builder()
                        .content(prompt)
                        .role(ChatMessageRole.USER)
                        .build())
                .build());

        return response.choices().get(0).message().content();
    }

    public static void main(String[] args) {
        try {
            GigaChatService service = new GigaChatService();

            String answer = service.sendMessage("Привет! Ответь в одном предложении.");
            System.out.println("Ответ: " + answer);

            String answer2 = service.sendMessage("Что такое искусственный интеллект?");
            System.out.println("Ответ 2: " + answer2);

        } catch (Exception e) {
            System.err.println("Ошибка при тесте:");
            e.printStackTrace();
        }
    }
}
