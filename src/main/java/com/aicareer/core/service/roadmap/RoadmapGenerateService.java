package com.aicareer.core.service.roadmap;

import com.aicareer.core.DTO.ResponseByWeek;
import com.aicareer.core.model.Roadmap;
import com.aicareer.core.model.RoadmapZone;
import com.aicareer.core.model.Task;
import com.aicareer.core.model.Week;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.roadmap.prompts.RoadmapPrompts;
import com.aicareer.repository.roadmap.RoadmapGenerate;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class RoadmapGenerateService implements RoadmapGenerate {

    private final GigaChatService gigaChatApiService;
    @Override
    public String gettingWeeksInformation(ResponseByWeek responseByWeek) {
        return responseByWeek.getWeeks().stream()
                .map(this::formatWeek)
                .collect(Collectors.joining("\n"));
    }

    private String formatWeek(Week week) {
        String weekHeader = "number of the week: " + week.getNumber() + "\n" +
                "goal for the week" + week.getNumber() + ": " + week.getGoal();

        String tasksInfo = week.getTasks().stream()
                .map(this::formatTask)
                .collect(Collectors.joining("\n"));

        String weekLinks = "Links for the week" + week.getNumber() + ": \n" +
                week.getTasks().stream()
                        .flatMap(task -> task.getUrls().stream())
                        .collect(Collectors.joining("\n"));

        return weekHeader + "\n" + tasksInfo + "\n" + weekLinks;
    }

    private String formatTask(Task task) {
        return "description for the task: " + "\n" +
                task.getDescription();
    }

    @Override
    public String informationComplexityAndQuantityAnalyzeAndCreatingZone(String weeksInformation) {

        String prompt = RoadmapPrompts.DIVISION_INTO_ZONES + weeksInformation;

        return gigaChatApiService.sendMessage(prompt);
    }

    @Override
    public List<RoadmapZone> splittingWeeksIntoZones(String resultOfComplexityAndQuantityAnalyze, List<Week> weeks) {

        List<String> roadmapZoneInString = List.of(resultOfComplexityAndQuantityAnalyze.split("\\*"));

        List<RoadmapZone> roadmapZones = new ArrayList<>();
        for (String zone : roadmapZoneInString) {
            List<String> zoneFieldsInString = List.of(zone.split("\\|"));

            RoadmapZone roadmapZone = new RoadmapZone();

            roadmapZone.setName(zoneFieldsInString.get(0).trim());
            roadmapZone.setLearningGoal(zoneFieldsInString.get(2).trim());
            roadmapZone.setComplexityLevel(zoneFieldsInString.get(3).trim());

            List<String> helperForRangeWeeks = List.of(zoneFieldsInString.get(1).trim().split(" "));

            int start = Integer.parseInt(helperForRangeWeeks.get(0));
            int end = Integer.parseInt(helperForRangeWeeks.get(1));

            roadmapZone.setWeeks(weeks.subList(start, end + 1));

            roadmapZones.add(roadmapZone);
        }
        return roadmapZones;
    }

    @Override
    public Roadmap identifyingThematicallySimilarZones(List<RoadmapZone> roadmapZones) {
        Roadmap roadmap = new Roadmap();
        for (RoadmapZone roadmapZone : roadmapZones) {
            roadmap.addRoadmapZone(roadmapZone);
        }
        return roadmap;
    }
}
