package com.aicareer.core.service.user;

import java.util.List;

public class UpdateResult {
  private final boolean success;
  private final List<String> errors;

  private UpdateResult(boolean success, List<String> errors) {
    this.success = success;
    this.errors = errors;
  }

  public static UpdateResult success() {
    return new UpdateResult(true, List.of());
  }

  public static UpdateResult error(List<String> errors) {
    return new UpdateResult(false, errors);
  }

  public static UpdateResult error(String error) {
    return new UpdateResult(false, List.of(error));
  }

  public boolean isSuccess() {
    return success;
  }

  public List<String> getErrors() {
    return errors;
  }

  public void printErrors() {
    if (!success) {
      System.out.println("❌ ОШИБКИ ОБНОВЛЕНИЯ:");
      errors.forEach(error -> System.out.println("   - " + error));
    }
  }
}