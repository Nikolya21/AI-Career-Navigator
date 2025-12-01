// com.aicareer.core.service.gigachat.GigaChatService
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
      .readTimeout(180)
      .authClient(AuthClient.builder()
        .withOAuth(AuthClientBuilder.OAuthBuilder.builder()
          .scope(Scope.GIGACHAT_API_PERS)
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
      // 🔑 Критически: параметры здесь — для всех вызовов
      .maxTokens(10000)
      .temperature(0.3f)
      .topP(0.9f)
      .repetitionPenalty(1.05f)
      .build());

    String content = response.choices().get(0).message().content();
    System.out.println("📊 Длина ответа GigaChat: " + (content != null ? content.length() : 0));
    return content != null ? content : "";
  }
}