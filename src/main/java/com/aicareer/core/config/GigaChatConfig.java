package com.aicareer.core.config;

public class GigaChatConfig {
  private final String clientId;
  private final String clientSecret;
  private final String scope;

  public GigaChatConfig() {
    this.clientId = System.getenv("GIGACHAT_CLIENT_ID");
    this.clientSecret = System.getenv("GIGACHAT_CLIENT_SECRET");
    this.scope = System.getenv("GIGACHAT_SCOPE");
  }

  public GigaChatConfig(String clientId, String clientSecret, String scope) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.scope = scope;
  }

  public String getClientId() { return clientId; }
  public String getClientSecret() { return clientSecret; }
  public String getScope() { return scope; }
}