package com.aicareer.module.roadmap;

import com.aicareer.core.model.RoadmapZone;
import com.aicareer.core.model.Week;

import java.util.List;

public interface RoadmapZonesCreate {

    String gettingWeeksInformation(List<Week> week);

    int informationComplexityAnalyze(String weeksInformation);

    int informationQuantityAnalyze(String weeksInformation);

    int calculatingTheNumberOfPointsPerZones(String resultOfComplexityAnalyze, String resultOfQuantityAnalyze);

    List<RoadmapZone> splittingWeeksIntoZones(int resultOfComplexityAnalyze,
                                              int resultOfQuantityAnalyze,
                                              int resultOfCalculatingTheNumberOfPointsPerZones,
                                              String weeksInformation);

}