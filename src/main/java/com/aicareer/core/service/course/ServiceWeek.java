package com.aicareer.core.service.course;

import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.repository.course.CourseResponse;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@AllArgsConstructor
@NoArgsConstructor
public class ServiceWeek implements CourseResponse {

  @Value("${course.bad-phrases}")
  private Set<String> badPhrases;

  @Value("${course.default-weeks:8}")
  private int defaultWeeks;

  @PostConstruct
  public void init() {
    log.info("ServiceWeek инициализирован:");
    log.info("   - Загружено bad-phrases: {} шт.", badPhrases.size());
    log.info("   - default-weeks: {}", defaultWeeks);

    if (badPhrases == null || badPhrases.isEmpty()) {
      log.warn("Список bad-phrases пуст! Проверь application.yml");
    }

    if (defaultWeeks < 1) {
      log.error("❌ default-weeks должен быть больше 0");
    }
  }

  @Override
  public List<Week> parseCourseResponse(String llmResponse) {
    log.info("📋 Начало парсинга строгого формата");

    if (llmResponse == null || llmResponse.trim().isEmpty()) {
      log.info("❌ Ответ пуст");
      return createFallbackWeeks();
    }

    // Быстрая проверка на плохой текст
    String lower = llmResponse.toLowerCase();
    for (String bad : badPhrases) {
      if (lower.contains(bad)) {
        log.info("🚨 Обнаружен недопустимый текст: '" + bad + "'");
        return createFallbackWeeks();
      }
    }

    // Очищаем ВСЕ, кроме нашего формата
    String cleanResponse = extractStrictFormat(llmResponse);

    if (cleanResponse.isEmpty()) {
      log.info("❌ Не найден строгий формат");
      log.info("📝 Полный ответ:\n" + llmResponse);
      return createFallbackWeeks();
    }

    List<Week> weeks = parseStrictFormat(cleanResponse);

    if (weeks.isEmpty()) {
      log.info("❌ Ошибка парсинга строгого формата");
      return createFallbackWeeks();
    }

    // Гарантируем 8 недель
    while (weeks.size() < defaultWeeks) {
      weeks.add(createDefaultWeek(weeks.size() + 1));
    }
    if (weeks.size() > defaultWeeks) {
      weeks = new ArrayList<>(weeks.subList(0, defaultWeeks));
    }

    log.info("✅ Успешно распарсено: " + weeks.size() + " недель");
    return weeks;
  }

  private String extractStrictFormat(String response) {
    // Ищем блоки с правильными делиметрими, игнорируя возможные ###
    Pattern formatPattern = Pattern.compile(
      "(?s).*?(?:^|\\s)[#\\s]*===WEEK_START===[#\\s]*(.*?)[#\\s]*===WEEK_END===[#\\s]*(?:$|\\s)",
      Pattern.MULTILINE
    );
    Matcher matcher = formatPattern.matcher(response);

    StringBuilder strictContent = new StringBuilder();
    while (matcher.find()) {
      String block = matcher.group(1).trim();
      if (!block.isEmpty()) {
        strictContent.append("===WEEK_START===\n")
          .append(block)
          .append("\n===WEEK_END===\n");
      }
    }

    return strictContent.toString().trim();
  }

  private List<Week> parseStrictFormat(String content) {
    List<Week> weeks = new ArrayList<>();
    Pattern weekPattern = Pattern.compile("===WEEK_START===(.*?)===WEEK_END===", Pattern.DOTALL);
    Matcher weekMatcher = weekPattern.matcher(content);

    while (weekMatcher.find()) {
      String weekContent = weekMatcher.group(1).trim();
      if (weekContent.isEmpty()) continue;

      try {
        Week week = parseWeekBlock(weekContent);
        if (week != null) {
          weeks.add(week);
        }
      } catch (Exception e) {
        log.error("❌ Ошибка парсинга недели: " + e.getMessage());
      }
    }

    return weeks;
  }

  private Week parseWeekBlock(String weekContent) {
    Week week = new Week();
    List<Task> tasks = new ArrayList<>();

    // Извлекаем номер недели
    Pattern numberPattern = Pattern.compile("NUMBER:(\\d+)");
    Matcher numberMatcher = numberPattern.matcher(weekContent);
    if (!numberMatcher.find()) {
      log.error("❌ Не найден номер недели");
      return null;
    }

    int weekNumber = Integer.parseInt(numberMatcher.group(1));
    week.setNumber(weekNumber);

    // Извлекаем цель
    Pattern goalPattern = Pattern.compile("GOAL:([^\n]+)");
    Matcher goalMatcher = goalPattern.matcher(weekContent);
    if (goalMatcher.find()) {
      String goal = goalMatcher.group(1).trim();
      if (!goal.isEmpty()) {
        week.setGoal(goal);
      } else {
        week.setGoal("Неделя " + weekNumber);
      }
    } else {
      week.setGoal("Неделя " + weekNumber);
    }

    // Извлекаем задачи
    Pattern taskPattern = Pattern.compile("===TASK_START===(.*?)===TASK_END===", Pattern.DOTALL);
    Matcher taskMatcher = taskPattern.matcher(weekContent);

    while (taskMatcher.find()) {
      Task task = parseTaskBlock(taskMatcher.group(1).trim());
      if (task != null) {
        tasks.add(task);
      }
    }

    // Гарантируем минимум 2 задачи
    if (tasks.size() < 2) {
      while (tasks.size() < 2) {
        tasks.add(createFallbackTask(weekNumber, tasks.size() + 1));
      }
    }

    week.setTasks(tasks);
//    System.out.println("✅ Распарсена неделя " + weekNumber + " с " + tasks.size() + " задачами");
    return week;
  }

  private Task parseTaskBlock(String taskContent) {
    Task task = new Task();

    // Извлекаем описание
    Pattern descPattern = Pattern.compile("DESCRIPTION:([^\n]+)");
    Matcher descMatcher = descPattern.matcher(taskContent);
    if (descMatcher.find()) {
      String description = descMatcher.group(1).trim();
      if (description.isEmpty()) return null;
      task.setDescription(description);
    } else {
      return null;
    }

    // Извлекаем ресурсы (без URL)
    Pattern resourcesPattern = Pattern.compile("RESOURCES:([^\n]+)");
    Matcher resourcesMatcher = resourcesPattern.matcher(taskContent);
    List<String> resources = new ArrayList<>();

    if (resourcesMatcher.find()) {
      String resourcesString = resourcesMatcher.group(1).trim();
      resources = parseResources(resourcesString);
    }

    // Если ресурсов нет или меньше 2, создаем заглушки
    if (resources.size() < 2) {
      resources = createFallbackResources(task.getDescription());
    }

    task.setUrls(resources);
    return task;
  }

  private List<String> parseResources(String resourcesString) {
    List<String> resources = new ArrayList<>();
    if (resourcesString == null || resourcesString.trim().isEmpty()) {
      return resources;
    }

    // Разделяем по запятой
    String[] resourceArray = resourcesString.split(",");
    for (String resource : resourceArray) {
      String cleanResource = resource.trim();
      if (!cleanResource.isEmpty() && !cleanResource.startsWith("http")) {
        resources.add(cleanResource);
      }
    }

    return resources;
  }

  private Task createFallbackTask(int weekNumber, int taskNumber) {
    Task task = new Task();
    task.setDescription("Задание " + taskNumber + " для недели " + weekNumber);
    task.setUrls(createFallbackResources(task.getDescription()));
    return task;
  }

  private List<String> createFallbackResources(String description) {
    return List.of(
      "Книга «Учебное пособие» автор А. Б. В. (главы 1-2)",
      "Видео «Лекция по теме» на Rutube канал «Образование»"
    );
  }

  private Week createDefaultWeek(int weekNumber) {
    Week week = new Week();
    week.setNumber(weekNumber);
    week.setGoal("Неделя " + weekNumber);
    week.setTasks(List.of(
      createFallbackTask(weekNumber, 1),
      createFallbackTask(weekNumber, 2)
    ));
    return week;
  }

  private List<Week> createFallbackWeeks() {
    log.info("🔄 Создание запасного плана");
    List<Week> weeks = new ArrayList<>();

    String[] goals = {
      "Введение и основы",
      "Основные концепции",
      "Практическое применение",
      "Углубленное изучение",
      "Работа с инструментами",
      "Решение задач",
      "Проектная работа",
      "Финальное закрепление"
    };

    for (int i = 0; i < defaultWeeks; i++) {
      Week week = new Week();
      week.setNumber(i + 1);
      week.setGoal(goals[i]);

      List<Task> tasks = new ArrayList<>();
      Task task1 = new Task();
      task1.setDescription("Теоретическое изучение материалов");
      task1.setUrls(List.of(
        "Книга «Основы темы» автор С. И. Петров (главы " + (i+1) + "-" + (i+2) + ")",
        "Видео «Лекция по теме " + (i+1) + "» на Rutube канал «Образование»"
      ));

      Task task2 = new Task();
      task2.setDescription("Практическое задание");
      task2.setUrls(List.of(
        "Статья «Практические примеры» на Хабр.ru",
        "Курс «Практикум» на Stepik.org"
      ));

      tasks.add(task1);
      tasks.add(task2);
      week.setTasks(tasks);
      weeks.add(week);
    }

    return weeks;
  }
}