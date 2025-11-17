package com.aicareer.repository.roadmap;

import com.aicareer.core.model.roadmap.Roadmap;
import java.util.List;
import java.util.Optional;

public interface RoadmapRepository {
    Roadmap save(Roadmap roadmap);
    Optional<Roadmap> findById(Long id);
    Optional<Roadmap> findByUserId(Long userId);
    List<Roadmap> findAllByUserId(Long userId);
    boolean delete(Long id);
    boolean deleteByUserId(Long userId);
}