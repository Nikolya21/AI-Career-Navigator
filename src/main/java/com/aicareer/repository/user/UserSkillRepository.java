package com.aicareer.repository.user;

import com.aicareer.core.model.user.UserSkills;

public interface UserSkillRepository {
  void save(UserSkills skills);
  UserSkills findByUserId(Long userId);
  void deleteByUserId(Long id);
}