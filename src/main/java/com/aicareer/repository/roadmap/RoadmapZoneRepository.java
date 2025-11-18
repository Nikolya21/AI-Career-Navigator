package com.aicareer.repository.roadmap;

import com.aicareer.core.model.roadmap.RoadmapZone;
import java.util.List;
import java.util.Optional;

public interface RoadmapZoneRepository {
    RoadmapZone save(RoadmapZone zone);
    List<RoadmapZone> findByRoadmapId(Long roadmapId);
    Optional<RoadmapZone> findById(Long id);
    boolean delete(Long id);
    boolean deleteByRoadmapId(Long roadmapId);
}