// com.aicareer.core.exception.CourseDefinitionException.java
package com.aicareer.core.exception;

/**
 * Исключение, возникающее при ошибках формирования требований к учебному курсу.
 */
public class CourseDefinitionException extends BusinessException {

  public CourseDefinitionException(String message) {
    super(message);
  }

  public CourseDefinitionException(String message, Throwable cause) {
    super(message, cause);
  }

  // Типы ошибок определения курса
  public enum Type {
    INSUFFICIENT_DATA,
    INCONSISTENT_REQUIREMENTS,
    COURSE_GENERATION_FAILED,
    TIME_ALLOCATION_IMPOSSIBLE
  }

  private CourseDefinitionException.Type type;

  public CourseDefinitionException(Type type, String message) {
    super(message);
    this.type = type;
  }

  public CourseDefinitionException(Type type, String message, Throwable cause) {
    super(message, cause);
    this.type = type;
  }

  public Type getType() {
    return type;
  }
}