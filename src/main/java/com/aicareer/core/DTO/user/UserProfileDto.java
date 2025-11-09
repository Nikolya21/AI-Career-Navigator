package com.aicareer.core.DTO.user;

import com.aicareer.core.model.user.CVData;
import com.aicareer.core.model.user.UserSkills;
import lombok.Data;

@Data
public class UserProfileDto {
  private String email;
  private String name;
  private CVData CV;
  private UserSkills skills;
}