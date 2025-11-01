package com.aicareer.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Roadmap {
    private int sid;
    private String name;
    private List<RoadmapZone> roadmapZones;
}
