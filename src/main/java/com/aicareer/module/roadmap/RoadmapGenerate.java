package com.aicareer.module.roadmap;

import com.aicareer.core.model.Roadmap;
import com.aicareer.core.model.RoadmapZone;

import java.util.List;

public interface RoadmapGenerate {

    Roadmap identifyingThematicallySimilarZones(List<RoadmapZone> roadmapZones);
}
