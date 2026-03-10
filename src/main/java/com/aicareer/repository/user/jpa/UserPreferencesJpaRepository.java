package com.aicareer.repository.user.jpa;

import com.aicareer.core.model.user.entity.UserPreferencesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferencesJpaRepository extends JpaRepository<UserPreferencesEntity, Long> {
  Optional<UserPreferencesEntity> findByUser_Id(Long userId);
  boolean existsByUser_Id(Long userId);
}