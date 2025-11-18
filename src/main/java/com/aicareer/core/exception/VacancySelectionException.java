// com.aicareer.core.exception.VacancySelectionException.java
package com.aicareer.core.exception;

/**
 * Исключение, возникающее при ошибках подбора или анализа вакансий.
 */
public class VacancySelectionException extends BusinessException {

  public VacancySelectionException(String message) {
    super(message);
  }

  public VacancySelectionException(String message, Throwable cause) {
    super(message, cause);
  }

  // Типы ошибок подбора вакансий
  public enum Type {
    NO_VACANCIES_FOUND,
    PARSING_FAILED,
    INVALID_PREFERENCES,
    TOO_MANY_REQUESTS,
    VACANCY_NOT_SELECTED
  }

  private VacancySelectionException.Type type;

  public VacancySelectionException(Type type, String message) {
    super(message);
    this.type = type;
  }

  public VacancySelectionException(Type type, String message, Throwable cause) {
    super(message, cause);
    this.type = type;
  }

  public Type getType() {
    return type;
  }
}