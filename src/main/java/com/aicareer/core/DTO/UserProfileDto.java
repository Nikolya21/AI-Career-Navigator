package com.aicareer.core.model.user;

import lombok.Data;

@Data
public class UserProfileDto {
  private String email;
  private String name;
  private CVData cv;
  private UserSkills skills;
}