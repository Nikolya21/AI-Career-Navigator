package com.aicareer.core.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Основная сущность пользователя
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
  private Long id;
  private String name;
  private String email;
  private String passwordHash; // Храним хеш пароля
  private CVData CV;
  private UserSkills skills;
  private String vacancyNow; // Получаю всю информацию про уже выбранную вакансию
}
