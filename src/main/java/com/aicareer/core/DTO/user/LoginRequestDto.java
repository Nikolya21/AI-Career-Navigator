package com.aicareer.core.DTO.user;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequestDto {
  @NotBlank
  private String email;

  @NotBlank
  private String password;
}