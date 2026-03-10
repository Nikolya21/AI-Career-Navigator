package com.aicareer.repository.user.jpa;

import com.aicareer.core.model.user.entity.UserSkillsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSkillsJpaRepository extends JpaRepository<UserSkillsEntity, Long> {
  Optional<UserSkillsEntity> findByUser_Id(Long userId);
}