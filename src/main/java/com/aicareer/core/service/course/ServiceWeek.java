package com.aicareer.core.service.course;

import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.repository.course.CourseResponse;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ServiceWeek implements CourseResponse {

  @Override
  public List<Week> parseCourseResponse(String llmResponse) {
    if (llmResponse == null || llmResponse.trim().isEmpty()) {
      System.out.println("❌ LLM response is empty");
      return createFallbackWeeks();
    }

    System.out.println("📋 Raw LLM response:\n" + llmResponse);

    List<Week> weeks = parseWeeksFromResponse(llmResponse);

    // Если не нашли недель, пробуем альтернативный парсинг
    if (weeks.isEmpty()) {
      System.out.println("⚠️ Primary parsing failed, trying alternative...");
      weeks = parseWeeksAlternative(llmResponse);
    }

    // Если все еще пусто, создаем fallback
    if (weeks.isEmpty()) {
      System.out.println("❌ No weeks found in LLM response, creating fallback");
      return createFallbackWeeks();
    }

    // Сортируем и валидируем недели
    weeks.sort(Comparator.comparingInt(Week::getNumber));
    validateAndFixWeeks(weeks);

    System.out.println("=== PARSING RESULT ===");
    System.out.println("Total weeks: " + weeks.size());

    for (Week week : weeks) {
      System.out.println("Week " + week.getNumber() + ": " + week.getGoal());
      System.out.println("Tasks: " + week.getTasks().size());
    }

    return weeks;
  }

  private List<Week> parseWeeksFromResponse(String llmResponse) {
    List<Week> weeks = new ArrayList<>();

    // Основной паттерн для поиска недель
    Pattern weekPattern = Pattern.compile("week(\\d+):\\s*goal:\"([^\"]*)\"((?:\\s*task\\d+:\"[^\"]*\"\\s*urls:\"[^\"]*\")+)", Pattern.MULTILINE);
    Matcher weekMatcher = weekPattern.matcher(llmResponse);

    while (weekMatcher.find()) {
      try {
        int weekNumber = Integer.parseInt(weekMatcher.group(1));
        String goal = weekMatcher.group(2);
        String tasksBlock = weekMatcher.group(3);

        List<Task> tasks = parseTasksFromBlock(tasksBlock);

        // Гарантируем хотя бы одну задачу
        if (tasks.isEmpty()) {
          tasks = createDefaultTask(weekNumber);
        }

        Week week = new Week();
        week.setNumber(weekNumber);
        week.setGoal(goal);
        week.setTasks(tasks);

        weeks.add(week);
        System.out.println("✅ Parsed week " + weekNumber);

      } catch (Exception e) {
        System.out.println("❌ Error parsing week: " + e.getMessage());
      }
    }

    return weeks;
  }

  private List<Week> parseWeeksAlternative(String llmResponse) {
    List<Week> weeks = new ArrayList<>();
    String[] lines = llmResponse.split("\n");

    int currentWeek = 1;
    for (String line : lines) {
      line = line.trim();
      if (line.startsWith("week") || line.matches("^\\d+[.:].*")) {
        try {
          Week week = parseSimpleWeekLine(line, currentWeek);
          if (week != null) {
            weeks.add(week);
            currentWeek++;
          }
        } catch (Exception e) {
          System.out.println("❌ Alternative parsing failed for: " + line);
        }
      }
    }

    return weeks;
  }

  private Week parseSimpleWeekLine(String line, int weekNumber) {
    // Упрощенный парсинг для строк типа "week1: goal:"...""
    String goal = extractSimpleGoal(line);
    if (goal == null) {
      goal = "Изучение материалов недели " + weekNumber;
    }

    Week week = new Week();
    week.setNumber(weekNumber);
    week.setGoal(goal);
    week.setTasks(createDefaultTask(weekNumber));

    return week;
  }

  private String extractSimpleGoal(String line) {
    try {
      // Пытаемся извлечь цель разными способами
      Pattern[] patterns = {
          Pattern.compile("goal:\"([^\"]*)\""),
          Pattern.compile("цель[^\"]*\"([^\"]*)\""),
          Pattern.compile("[^:]*:\\s*(.*)")
      };

      for (Pattern pattern : patterns) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
          String goal = matcher.group(1).trim();
          if (!goal.isEmpty() && !goal.equals("\"") && !goal.matches(".*[tT]ask.*")) {
            return goal;
          }
        }
      }
    } catch (Exception e) {
      System.out.println("❌ Error extracting goal: " + e.getMessage());
    }
    return null;
  }

  private List<Task> parseTasksFromBlock(String tasksBlock) {
    List<Task> tasks = new ArrayList<>();

    try {
      Pattern taskPattern = Pattern.compile("task(\\d+):\"([^\"]*)\"\\s*urls:\"([^\"]*)\"");
      Matcher taskMatcher = taskPattern.matcher(tasksBlock);

      while (taskMatcher.find()) {
        String description = taskMatcher.group(2);
        String urlsStr = taskMatcher.group(3);

        if (description != null && !description.trim().isEmpty()) {
          Task task = new Task();
          task.setDescription(description.trim());

          List<String> urls = Arrays.stream(urlsStr.split(","))
              .filter(url -> !url.trim().isEmpty())
              .map(String::trim)
              .collect(Collectors.toList());

          task.setUrls(urls);
          tasks.add(task);
        }
      }
    } catch (Exception e) {
      System.out.println("❌ Error parsing tasks: " + e.getMessage());
    }

    return tasks;
  }

  private List<Task> createDefaultTask(int weekNumber) {
    Task task = new Task();
    task.setDescription("Изучить материалы и выполнить практические задания недели " + weekNumber);
    task.setUrls(List.of("https://example.com/week" + weekNumber));
    return List.of(task);
  }

  private void validateAndFixWeeks(List<Week> weeks) {
    // Убеждаемся, что номера недель уникальны и последовательны
    Set<Integer> weekNumbers = new HashSet<>();
    List<Week> validWeeks = new ArrayList<>();

    for (Week week : weeks) {
      if (week.getNumber() > 0 && !weekNumbers.contains(week.getNumber())) {
        weekNumbers.add(week.getNumber());
        validWeeks.add(week);
      }
    }

    weeks.clear();
    weeks.addAll(validWeeks);

    // Пересчитываем номера чтобы они были последовательными
    for (int i = 0; i < weeks.size(); i++) {
      weeks.get(i).setNumber(i + 1);
    }
  }

  private List<Week> createFallbackWeeks() {
    System.out.println("🔄 Creating guaranteed fallback curriculum");

    List<Week> weeks = new ArrayList<>();

    // Создаем гарантированный учебный план из 4 недель
    for (int i = 1; i <= 4; i++) {
      Week week = new Week();
      week.setNumber(i);
      week.setGoal("Освоение ключевых концепций - неделя " + i);
      week.setTasks(createDefaultTask(i));
      weeks.add(week);
    }

    return weeks;
  }
}