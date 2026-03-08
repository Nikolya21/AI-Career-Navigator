package com.aicareer.core.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для данных, полученных от GigaChat после анализа диалога.
 * Содержит только учебные предпочтения пользователя.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLearningProfile {
  private String weeklyHours;
  private String preferredFormat;
  private String motivation;
  private String background;
  private String motivationLevel;
}