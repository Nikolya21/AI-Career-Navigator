package com.aicareer.core.service.roadmap;

import com.aicareer.core.DTO.courseDto.ResponseByWeek;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.model.courseModel.Week;
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

    public RoadmapGenerateService(GigaChatService gigaChatApiService) {
        this.gigaChatApiService = gigaChatApiService;
    }
    @Override
    public String gettingWeeksInformation(ResponseByWeek responseByWeek) {
        return responseByWeek.getWeeks().size() + "\n" +responseByWeek.getWeeks().stream()
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
        int quantityOfWeeks = Integer.parseInt(weeksInformation.split("\n")[0].trim());
        RoadmapPrompts.setQuantityOfWeeks(quantityOfWeeks);

        String prompt = RoadmapPrompts.DIVISION_INTO_ZONES + weeksInformation;
        return gigaChatApiService.sendMessage(prompt);
    }

    @Override
    public List<RoadmapZone> splittingWeeksIntoZones(String resultOfComplexityAndQuantityAnalyze, List<Week> weeks) {
        // Очищаем ответ от возможных квадратных скобок в начале и конце
        String cleanedResponse = resultOfComplexityAndQuantityAnalyze.trim();
        if (cleanedResponse.startsWith("[") && cleanedResponse.endsWith("]")) {
            cleanedResponse = cleanedResponse.substring(1, cleanedResponse.length() - 1).trim();
        }

        // Разделяем на зоны по разделителю "---"
        List<String> roadmapZoneInString = List.of(cleanedResponse.split("---"));
        System.out.println("Found zones: " + roadmapZoneInString.size());

        List<RoadmapZone> roadmapZones = new ArrayList<>();

        for (String zone : roadmapZoneInString) {
            if (zone.trim().isEmpty()) continue;

            System.out.println("Processing zone: " + zone.substring(0, Math.min(50, zone.length())) + "...");

            RoadmapZone roadmapZone = new RoadmapZone();

            // Парсим каждое поле зоны
            String name = extractFieldValue(zone, "НАЗВАНИЕ:");
            String weeksRange = extractFieldValue(zone, "НЕДЕЛИ:");
            String skills = extractFieldValue(zone, "НАВЫКИ:");
            String complexity = extractFieldValue(zone, "СЛОЖНОСТЬ:");

            // Устанавливаем значения
            roadmapZone.setName(name);
            roadmapZone.setLearningGoal(skills);
            roadmapZone.setComplexityLevel(complexity);

            // Обрабатываем диапазон недель
            if (weeksRange != null) {
                List<String> weekNumbers = List.of(weeksRange.trim().split("\\s+"));
                if (weekNumbers.size() >= 2) {
                    try {
                        int start = Integer.parseInt(weekNumbers.get(0));
                        int end = Integer.parseInt(weekNumbers.get(1));

                        // Проверяем валидность диапазона
                        if (start >= 1 && end <= weeks.size() && start <= end) {
                            roadmapZone.setWeeks(weeks.subList(start - 1, end));
                        } else {
                            System.err.println("Invalid week range: " + start + "-" + end);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing week numbers: " + weeksRange);
                    }
                }
            }

            roadmapZones.add(roadmapZone);
        }

        System.out.println("Processed zones: " + roadmapZones.size());
        return roadmapZones;
    }

    // Вспомогательный метод для извлечения значений полей
    private String extractFieldValue(String zoneText, String fieldName) {
        int fieldIndex = zoneText.indexOf(fieldName);
        if (fieldIndex == -1) {
            return null;
        }

        int valueStart = fieldIndex + fieldName.length();
        int valueEnd = zoneText.indexOf('\n', valueStart);

        if (valueEnd == -1) {
            valueEnd = zoneText.length();
        }

        String value = zoneText.substring(valueStart, valueEnd).trim();

        // Убираем возможные кавычки и лишние символы
        value = value.replaceAll("^[\"']|[\"']$", "");

        return value;
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
