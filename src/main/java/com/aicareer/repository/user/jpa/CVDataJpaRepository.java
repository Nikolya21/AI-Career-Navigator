package com.aicareer.repository.user.jpa;

import com.aicareer.core.model.user.entity.CVDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CVDataJpaRepository extends JpaRepository<CVDataEntity, Long> {
  Optional<CVDataEntity> findByUser_Id(Long userId);
}