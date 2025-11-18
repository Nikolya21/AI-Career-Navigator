package com.aicareer.core.validator.user;

import com.aicareer.core.dto.user.UserRegistrationDto;
import com.aicareer.core.validator.util.ValidationUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RegistrationValidator {

  public static List<String> validate(UserRegistrationDto dto, Function<String, Boolean> emailAvailabilityChecker) {
    List<String> errors = new ArrayList<>();

    try {
      ValidationUtil.validateEmail(dto.getEmail(), "Email");
    } catch (ValidationException e) {
      errors.add(e.getMessage());
    }

    try {
      ValidationUtil.validateName(dto.getName(), "Name");
    } catch (ValidationException e) {
      errors.add(e.getMessage());
    }

    try {
      ValidationUtil.validatePassword(dto.getPassword(), "Password");
    } catch (ValidationException e) {
      errors.add(e.getMessage());
    }

    if (errors.isEmpty() && !emailAvailabilityChecker.apply(dto.getEmail())) {
      errors.add("Email already taken");
    }

    return errors;
  }
}