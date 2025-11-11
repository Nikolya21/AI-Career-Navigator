package com.aicareer.core.service.user;

import com.aicareer.core.model.user.User;
import java.util.List;

public class RegistrationResult {
  private final boolean success;
  private final User user;
  private final List<String> errors;

  private RegistrationResult(boolean success, User user, List<String> errors) {
    this.success = success;
    this.user = user;
    this.errors = errors;
  }

  public static RegistrationResult success(User user) {
    return new RegistrationResult(true, user, List.of());
  }

  public static RegistrationResult error(List<String> errors) {
    return new RegistrationResult(false, null, errors);
  }

  public boolean isSuccess() {
    return success;
  }

  public User getUser() {
    return user;
  }

  public List<String> getErrors() {
    return errors;
  }

  public void printErrors() {
    if (!success) {
      System.out.println("❌ ОШИБКИ РЕГИСТРАЦИИ:");
      errors.forEach(error -> System.out.println("   - " + error));
    }
  }
}