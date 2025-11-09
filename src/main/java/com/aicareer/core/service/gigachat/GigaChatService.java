package com.aicareer.core.service.gigachat;

import chat.giga.client.GigaChatClient;
import chat.giga.client.auth.AuthClient;
import chat.giga.client.auth.AuthClientBuilder;
import chat.giga.model.ModelName;
import chat.giga.model.Scope;
import chat.giga.model.completion.ChatMessage;
import chat.giga.model.completion.CompletionRequest;
import chat.giga.model.completion.CompletionResponse;
import ru.sberbank.gigachat.*;

import javax.management.relation.Role;

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
                .model(ModelName.GIGA_CHAT_MAX)
                .message(ChatMessage.builder()
                        .content(prompt)
                        .role(Role.USER)
                        .build())
                .build());

        return response.getChoices().get(0).getMessage().getContent();
    }
}
