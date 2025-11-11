package com.aicareer.core.validation;

import com.aicareer.core.DTO.LoginRequestDto;
import java.util.ArrayList;
import java.util.List;

public class AuthenticationValidator {

  public static List<String> validate(LoginRequestDto dto) {
    List<String> errors = new ArrayList<>();

    try {
      ValidationUtils.validateEmail(dto.getEmail(), "Email");
    } catch (ValidationException e) {
      errors.add(e.getMessage());
    }

    if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
      errors.add("Password is required");
    }

    return errors;
  }
}