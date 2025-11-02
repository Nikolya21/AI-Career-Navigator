package com.aicareer.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Roadmap {
    private List<RoadmapZone> roadmapZones;

    public void addRoadmapZone(RoadmapZone zone) {
        if (this.roadmapZones == null) {
            this.roadmapZones = new ArrayList<>();
        }
        this.roadmapZones.add(zone);
    }
}
