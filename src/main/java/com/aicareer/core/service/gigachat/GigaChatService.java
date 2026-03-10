package com.aicareer.core.service.gigachat;

import com.aicareer.core.config.GigaChatConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import chat.giga.client.GigaChatClient;
import chat.giga.client.auth.AuthClient;
import chat.giga.client.auth.AuthClientBuilder;
import chat.giga.model.ModelName;
import chat.giga.model.Scope;
import chat.giga.model.completion.ChatMessage;
import chat.giga.model.completion.ChatMessageRole;
import chat.giga.model.completion.CompletionRequest;
import chat.giga.model.completion.CompletionResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class GigaChatService {

  private final GigaChatConfig config;

  public String sendMessage(String prompt) {
    // Преобразуем строку из конфига в enum Scope
    Scope scope = Scope.valueOf(config.getScope().toUpperCase());

    GigaChatClient client = GigaChatClient.builder()
        .verifySslCerts(false)
        .readTimeout(config.getTimeoutMillis())
        .authClient(AuthClient.builder()
            .withOAuth(AuthClientBuilder.OAuthBuilder.builder()
                .scope(scope)
                .authKey("MDE5YTQwOGYtMmUwOS03MDQ5LThjNzUtYTk1OTgzZjljNzE2OmRkNWYxZGU3LWJjN2QtNDFjOS04MzE0LTRmYjNkYWYyZWQ0ZA==")
                .build())
            .build())
        .build();

    CompletionResponse response = client.completions(CompletionRequest.builder()
        .model(ModelName.GIGA_CHAT)
        .message(ChatMessage.builder()
            .content(prompt)
            .role(ChatMessageRole.USER)
            .build())
        .maxTokens(12000)
        .temperature(0.3f)
        .topP(0.9f)
        .repetitionPenalty(1.05f)
        .build());

    String content = response.choices().get(0).message().content();
    log.info("📊 Длина ответа GigaChat: {}", content != null ? content.length() : 0);
    return content != null ? content : "";
  }
}