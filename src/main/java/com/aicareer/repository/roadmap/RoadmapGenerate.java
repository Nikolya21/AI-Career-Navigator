package com.aicareer.repository.roadmap;

import com.aicareer.core.model.Roadmap;
import com.aicareer.core.model.RoadmapZone;
import com.aicareer.core.model.Week;

import java.util.List;

public interface RoadmapGenerate {

    String gettingWeeksInformation(List<Week> weeks);

    String informationComplexityAndQuantityAnalyzeAndCreatingZone(String weeksInformation);

    List<RoadmapZone> splittingWeeksIntoZones(String resultOfComplexityAndQuantityAnalyzeAndCreatingZone,
                                              List<Week> weeks);

    Roadmap identifyingThematicallySimilarZones(List<RoadmapZone> roadmapZones);
}
