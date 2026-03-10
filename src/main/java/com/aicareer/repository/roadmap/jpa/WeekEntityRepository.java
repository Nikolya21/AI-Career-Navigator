package com.aicareer.repository.roadmap.jpa;

import com.aicareer.core.model.roadmap.entity.WeekEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeekEntityRepository extends JpaRepository<WeekEntity, Long> {
  List<WeekEntity> findByRoadmapZoneId(Long roadmapZoneId);
}