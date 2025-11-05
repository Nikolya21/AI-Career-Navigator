package com.aicareer.core.service.user;

import com.aicareer.core.model.user.UserSkills;
import com.aicareer.module.user.UserSkillRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserSkillRepositoryImpl implements UserSkillRepository {

  private final Map<Long, UserSkills> skillStore = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  @Override
  public void save(UserSkills skills) {
    if (skills.getId() == null) {
      skills.setId(idGenerator.getAndIncrement());
    }
    skillStore.put(skills.getUserId(), skills);
  }

  @Override
  public UserSkills findByUserId(Long userId) {
    return skillStore.get(userId);
  }

  @Override
  public void deleteByUserId(Long userId) {
    skillStore.remove(userId);
  }
}