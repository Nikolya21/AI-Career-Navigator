// com.aicareer.core.exception.ChatException.java
package com.aicareer.core.exception;

/**
 * Исключение, возникающее при ошибках взаимодействия с AI-сервисом (GigaChat).
 */
public class ChatException extends BusinessException {

  public ChatException(String message) {
    super(message);
  }

  public ChatException(String message, Throwable cause) {
    super(message, cause);
  }

  // Типы ошибок AI-чата
  public enum Type {
    API_UNAVAILABLE,
    RATE_LIMIT_EXCEEDED,
    INVALID_RESPONSE_FORMAT,
    PROMPT_TOO_LONG,
    CONVERSATION_TIMEOUT,
    MODEL_ERROR
  }

  private ChatException.Type type;

  public ChatException(Type type, String message) {
    super(message);
    this.type = type;
  }

  public ChatException(Type type, String message, Throwable cause) {
    super(message, cause);
    this.type = type;
  }

  public Type getType() {
    return type;
  }
}