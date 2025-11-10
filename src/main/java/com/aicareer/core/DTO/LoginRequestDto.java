package com.aicareer.core.DTO;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class LoginRequestDto {
  private String email;
  private String password;

  public List<String> validate() {
    List<String> errors = new ArrayList<>();

    if (email == null || email.trim().isEmpty()) {
      errors.add("EMAIL: Email не может быть пустым");
    } else if (!isValidEmail(email)) {
      errors.add("EMAIL: Неверный формат email");
    }

    if (password == null || password.isEmpty()) {
      errors.add("ПАРОЛЬ: Пароль не может быть пустым");
    }

    return errors;
  }

  private boolean isValidEmail(String email) {
    return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z]+\\.[A-Za-z]{2,}$");
  }
}