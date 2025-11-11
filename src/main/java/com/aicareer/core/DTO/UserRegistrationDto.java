package com.aicareer.core.DTO;

import lombok.Data;

@Data
public class UserRegistrationDto {
  private String email;
  private String password;
  private String name;
}