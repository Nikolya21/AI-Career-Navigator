package com.aicareer.core.validation;

import com.aicareer.core.DTO.user.LoginRequestDto;
import com.aicareer.core.validation.util.ValidationUtil;
import java.util.ArrayList;
import java.util.List;

public class AuthenticationValidator {

  public static List<String> validate(LoginRequestDto dto) {
    List<String> errors = new ArrayList<>();

    try {
      ValidationUtil.validateEmail(dto.getEmail(), "Email");
    } catch (ValidationException e) {
      errors.add(e.getMessage());
    }

    if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
      errors.add("Password is required");
    }

    return errors;
  }
}