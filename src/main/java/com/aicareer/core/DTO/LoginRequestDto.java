package com.aicareer.core.model.user;

import lombok.Data;

@Data
public class LoginRequestDto {
  private String email;
  private String password;
}