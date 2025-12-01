// com.aicareer.core.service.roadmap.RoadmapGenerateService
package com.aicareer.core.service.roadmap;

import com.aicareer.core.dto.courseDto.ResponseByWeek;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.roadmap.prompts.RoadmapPrompts;
import com.aicareer.repository.roadmap.RoadmapGenerate;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
    return responseByWeek.getWeeks().size() + "\n" +
      responseByWeek.getWeeks().stream()
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
  public List<RoadmapZone> splittingWeeksIntoZones(String zonesAnalysis, List<Week> allWeeks) {
    System.out.println("🔄 Распределение " + allWeeks.size() + " недель по зонам (алгоритмически)");

    if (allWeeks == null || allWeeks.isEmpty()) {
      throw new IllegalArgumentException("Список недель не может быть пустым");
    }

    // ✅ ИСПРАВЛЕНИЕ: создаём изменяемую копию перед сортировкой
    List<Week> mutableWeeks = new ArrayList<>(allWeeks);
    mutableWeeks.sort(Comparator.comparingInt(Week::getNumber));

    int total = mutableWeeks.size();
    // Рекомендуемое: 3 зоны для 6–8 недель, 4 зоны для 9–12
    int zonesCount = (total <= 6) ? 3 : (total <= 9 ? 4 : 5);
    zonesCount = Math.min(zonesCount, total); // не больше, чем недель

    List<RoadmapZone> zones = new ArrayList<>();
    int start = 0;

    for (int i = 0; i < zonesCount; i++) {
      int baseSize = total / zonesCount;
      int remainder = total % zonesCount;
      int size = baseSize + (i < remainder ? 1 : 0);
      int end = Math.min(start + size, total);

      if (start >= end) break;

      List<Week> zoneWeeks = new ArrayList<>(mutableWeeks.subList(start, end));
      RoadmapZone zone = createZoneFromWeeks(zoneWeeks, i + 1, zonesAnalysis);
      zones.add(zone);

      System.out.println("✅ Зона " + (i + 1) + ": недели " +
        zoneWeeks.stream().map(Week::getNumber).collect(Collectors.toList()));
      start = end;
    }

    return zones;
  }

  private RoadmapZone createZoneFromWeeks(List<Week> weeks, int zoneOrder, String zonesAnalysis) {
    RoadmapZone zone = new RoadmapZone();
    zone.setZoneOrder(zoneOrder);

    // Имя — из первой недели или из zonesAnalysis (если есть)
    String baseName = weeks.get(0).getGoal();
    String name = extractZoneName(baseName);
    zone.setName(name.isEmpty() ? "Зона " + zoneOrder : name);

    // Skills / learningGoal — объединяем цели
    String learningGoal = weeks.stream()
      .map(Week::getGoal)
      .filter(s -> s != null && !s.trim().isEmpty())
      .distinct()
      .collect(Collectors.joining("; "));
    zone.setLearningGoal(learningGoal.isEmpty() ? "Развитие профессиональных компетенций" : learningGoal);

    // Сложность — по номеру последней недели
    int maxWeek = weeks.stream().mapToInt(Week::getNumber).max().orElse(1);
    zone.setComplexityLevel(
      maxWeek <= 3 ? "начальный" :
        maxWeek <= 6 ? "средний" : "сложный"
    );

    zone.setWeeks(weeks);
    return zone;
  }

  private String extractZoneName(String goal) {
    if (goal == null) return "";
    // Убираем глаголы и оставляем суть
    String clean = goal
      .replaceAll("(?i)^[Оо]своить|[Нн]аучиться|[Иi]зучить|[Пп]олучить навыки|[Рr]азвить", "")
      .replaceAll("[.,;:!?]+$", "")
      .trim();
    String[] words = clean.split("\\s+");
    return words.length == 0 ? "" : String.join(" ",
      Arrays.copyOfRange(words, 0, Math.min(4, words.length)));
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