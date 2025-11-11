package com.aicareer.core.DTO;

import lombok.Data;

@Data
public class LoginRequestDto {
  private String email;
  private String password;
}