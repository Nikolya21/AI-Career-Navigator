package com.aicareer.core.DTO;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserRegistrationDto {
  private String email;
  private String password;
  private String name;

  public List<String> validate() {
    List<String> errors = new ArrayList<>();

    if (email == null || email.trim().isEmpty()) {
      errors.add("EMAIL: Email не может быть пустым");
    } else if (!isValidEmail(email)) {
      errors.add("EMAIL: Неверный формат email. Пример: user@example.com");
    }

    if (password == null || password.isEmpty()) {
      errors.add("ПАРОЛЬ: Пароль не может быть пустым");
    } else if (password.length() < 8) {
      errors.add("ПАРОЛЬ: Пароль должен содержать минимум 8 символов");
    } else if (!containsUpperCase(password)) {
      errors.add("ПАРОЛЬ: Пароль должен содержать хотя бы одну заглавную букву");
    } else if (!containsLowerCase(password)) {
      errors.add("ПАРОЛЬ: Пароль должен содержать хотя бы одну строчную букву");
    } else if (!containsDigit(password)) {
      errors.add("ПАРОЛЬ: Пароль должен содержать хотя бы одну цифру");
    }

    if (name == null || name.trim().isEmpty()) {
      errors.add("ИМЯ: Имя не может быть пустым");
    } else if (name.trim().length() < 2) {
      errors.add("ИМЯ: Имя должно содержать минимум 2 символа");
    } else if (!isValidName(name)) {
      errors.add("ИМЯ: Имя должно содержать только буквы, пробелы и дефисы");
    }

    return errors;
  }

  public List<String> validateEmailAvailability(EmailChecker emailChecker) {
    List<String> errors = validate();

    if (errors.isEmpty() && emailChecker != null && emailChecker.isEmailExists(email)) {
      errors.add("EMAIL: Этот email уже зарегистрирован");
    }

    return errors;
  }

  private boolean isValidEmail(String email) {
    return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z]+\\.[A-Za-z]{2,}$");
  }

  private boolean isValidName(String name) {
    return name.matches("^[a-zA-Zа-яА-ЯёЁ\\s-]+$");
  }

  private boolean containsUpperCase(String str) {
    return str.chars().anyMatch(Character::isUpperCase);
  }

  private boolean containsLowerCase(String str) {
    return str.chars().anyMatch(Character::isLowerCase);
  }

  private boolean containsDigit(String str) {
    return str.chars().anyMatch(Character::isDigit);
  }

  @FunctionalInterface
  public interface EmailChecker {
    boolean isEmailExists(String email);
  }
}