package com.aicareer.core.model.user;

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
  private CVData CV;
  private UserSkills skills;
  private String vacancyNow; // получаю всю информацию про уже выбранную вакансию от Сани
}
