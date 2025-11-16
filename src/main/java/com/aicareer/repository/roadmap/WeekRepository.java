package com.aicareer.repository.roadmap;

import com.aicareer.core.model.courseModel.Week;
import java.util.List;
import java.util.Optional;

public interface WeekRepository {
    Week save(Week week);
    List<Week> findByRoadmapZoneId(Long roadmapZoneId);
    Optional<Week> findById(Long id);
    boolean delete(Long id);
    boolean deleteByRoadmapZoneId(Long roadmapZoneId);
}