package com.aicareer.core.model;

import com.aicareer.core.model.RoadmapZone;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Roadmap {
    private Long id;
    private Long userId;
    private List<RoadmapZone> roadmapZones = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void addRoadmapZone(RoadmapZone zone) {
        if (this.roadmapZones == null) {
            this.roadmapZones = new ArrayList<>();
        }
        this.roadmapZones.add(zone);
    }
}
