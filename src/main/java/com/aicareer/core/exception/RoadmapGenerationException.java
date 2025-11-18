// com.aicareer.core.exception.RoadmapGenerationException.java
package com.aicareer.core.exception;

/**
 * Исключение, возникающее при ошибках генерации дорожной карты обучения.
 */
public class RoadmapGenerationException extends BusinessException {

  public RoadmapGenerationException(String message) {
    super(message);
  }

  public RoadmapGenerationException(String message, Throwable cause) {
    super(message, cause);
  }

  // Типы ошибок генерации дорожной карты
  public enum Type {
    WEEK_DISTRIBUTION_FAILED,
    VALIDATION_FAILED,
    MISSING_COURSE_DATA,
    INFRASTRUCTURE_ERROR
  }

  private RoadmapGenerationException.Type type;

  public RoadmapGenerationException(Type type, String message) {
    super(message);
    this.type = type;
  }

  public RoadmapGenerationException(Type type, String message, Throwable cause) {
    super(message, cause);
    this.type = type;
  }

  public Type getType() {
    return type;
  }
}