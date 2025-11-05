package com.aicareer.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoadmapZone {
    private String name;
    private List<Week> weeks;
    private String learningGoal;
    private String complexityLevel;
}
