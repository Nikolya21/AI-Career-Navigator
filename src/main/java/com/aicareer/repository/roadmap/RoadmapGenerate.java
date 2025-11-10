package com.aicareer.repository.roadmap;

import com.aicareer.core.DTO.courseDto.ResponseByWeek;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.core.model.courseModel.Week;

import java.util.List;

public interface RoadmapGenerate {


    String gettingWeeksInformation(ResponseByWeek responseByWeek);

    String informationComplexityAndQuantityAnalyzeAndCreatingZone(String weeksInformation);

    List<RoadmapZone> splittingWeeksIntoZones(String resultOfComplexityAndQuantityAnalyzeAndCreatingZone,
                                              List<Week> weeks);

    Roadmap identifyingThematicallySimilarZones(List<RoadmapZone> roadmapZones);
}
