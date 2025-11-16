package com.aicareer.repository.user;

import com.aicareer.core.model.user.UserSkills;
import java.util.Optional;

public interface UserSkillsRepository {
  UserSkills save(UserSkills skills);
  Optional<UserSkills> findByUserId(Long userId);
}