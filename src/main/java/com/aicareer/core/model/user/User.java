package com.aicareer.core.model.user;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// основная сущность пользователя
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
  private Long id;
  private String name;
  private String email;
  private String passwordHash; // храним хеш пароля
  private CVData cv;
  private UserSkills skills;
  private String vacancyNow; // получаю всю информацию про уже выбранную вакансию от Сани
  private UserPreferences userPreferences; // от Луки
  private Long roadmapId;
  private Instant createdAt;
  private Instant updatedAt;

  public void updateTimestamps() {
    // if (id == null) {
    //   createdAt = Instant.now();
    // }
    updatedAt = Instant.now();
  }
}
