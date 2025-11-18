// com.aicareer.core.exception.AuthenticationException.java
package com.aicareer.core.exception;

/**
 * Исключение, возникающее при ошибках аутентификации или регистрации пользователя.
 */
public class AuthenticationException extends BusinessException {

  public AuthenticationException(String message) {
    super(message);
  }

  public AuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }

  // Дополнительно: можно добавить enum для типов ошибок
  public enum Type {
    INVALID_CREDENTIALS,
    USER_ALREADY_EXISTS,
    ACCOUNT_LOCKED,
    INVALID_EMAIL_FORMAT,
    WEAK_PASSWORD
  }

  private AuthenticationException.Type type;

  public AuthenticationException(Type type, String message) {
    super(message);
    this.type = type;
  }

  public AuthenticationException(Type type, String message, Throwable cause) {
    super(message, cause);
    this.type = type;
  }

  public Type getType() {
    return type;
  }
}