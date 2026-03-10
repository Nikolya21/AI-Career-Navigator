package com.aicareer.repository.roadmap.jpa;

import com.aicareer.core.model.roadmap.entity.RoadmapZoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapZoneEntityRepository extends JpaRepository<RoadmapZoneEntity, Long> {
  List<RoadmapZoneEntity> findByRoadmapId(Long roadmapId);
}