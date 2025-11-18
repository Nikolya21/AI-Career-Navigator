// com.aicareer.core.exception.BusinessException.java
package com.aicareer.core.exception;

/**
 * Базовое checked-исключение для всех бизнес-ошибок.
 * Используется, когда ошибка предсказуема и может быть обработана пользователем или системой.
 */
public abstract class BusinessException extends Exception {

  public BusinessException(String message) {
    super(message);
  }

  public BusinessException(String message, Throwable cause) {
    super(message, cause);
  }
}