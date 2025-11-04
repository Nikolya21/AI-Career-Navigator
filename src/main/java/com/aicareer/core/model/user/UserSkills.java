package com.aicareer.core.model.user;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Данные обучения, прогресс
public class UserSkills {
  private Long id;
  private Long userId;
  private double FullCompliancePercentage;
  private Map<String, Double> skillGaps; // Проценты по навыкам
  private LocalDateTime calculatedAt;

  public void updateTimestamps() {
    calculatedAt = LocalDateTime.now();
  }
}
