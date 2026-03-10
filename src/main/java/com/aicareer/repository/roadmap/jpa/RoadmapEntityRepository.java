package com.aicareer.repository.roadmap.jpa;

import com.aicareer.core.model.roadmap.entity.RoadmapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoadmapEntityRepository extends JpaRepository<RoadmapEntity, Long> {
  Optional<RoadmapEntity> findByUserId(Long userId);
}