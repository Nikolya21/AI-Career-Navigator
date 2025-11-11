package com.aicareer.core.validation;

import com.aicareer.core.DTO.UserRegistrationDto;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RegistrationValidator {

  public static List<String> validate(UserRegistrationDto dto, Function<String, Boolean> emailAvailabilityChecker) {
    List<String> errors = new ArrayList<>();

    try {
      ValidationUtils.validateEmail(dto.getEmail(), "Email");
    } catch (ValidationException e) {
      errors.add(e.getMessage());
    }

    try {
      ValidationUtils.validateName(dto.getName(), "Name");
    } catch (ValidationException e) {
      errors.add(e.getMessage());
    }

    try {
      ValidationUtils.validatePassword(dto.getPassword(), "Password");
    } catch (ValidationException e) {
      errors.add(e.getMessage());
    }

    if (errors.isEmpty() && !emailAvailabilityChecker.apply(dto.getEmail())) {
      errors.add("Email already taken");
    }

    return errors;
  }
}