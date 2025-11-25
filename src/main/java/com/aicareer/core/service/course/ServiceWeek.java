package com.aicareer.core.service.course;

import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.repository.course.CourseResponse;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceWeek implements CourseResponse {

  @Override
  public List<Week> parseCourseResponse(String llmResponse) {
    System.out.println("📋 Начало парсинга строгого формата");

    if (llmResponse == null || llmResponse.trim().isEmpty()) {
      System.out.println("❌ Ответ пуст");
      return createFallbackWeeks();
    }

    // Очищаем ВСЕ, кроме нашего формата
    String cleanResponse = extractStrictFormat(llmResponse);

    if (cleanResponse.isEmpty()) {
      System.out.println("❌ Не найден строгий формат");
      return createFallbackWeeks();
    }

    List<Week> weeks = parseStrictFormat(cleanResponse);

    if (weeks.isEmpty()) {
      System.out.println("❌ Ошибка парсинга строгого формата");
      return createFallbackWeeks();
    }

    // Гарантируем 8 недель
    while (weeks.size() < 8) {
      weeks.add(createDefaultWeek(weeks.size() + 1));
    }
    if (weeks.size() > 8) {
      weeks = weeks.subList(0, 8);
    }

    System.out.println("✅ Успешно распаршено: " + weeks.size() + " недель");
    return weeks;
  }

  /**
   * Извлекает только данные в строгом формате, отбрасывает всё остальное
   */
  private String extractStrictFormat(String response) {
    // Ищем блок между первым WEEK_START и последним WEEK_END
    Pattern formatPattern = Pattern.compile("(WEEK_START.*?WEEK_END)", Pattern.DOTALL);
    Matcher matcher = formatPattern.matcher(response);

    StringBuilder strictContent = new StringBuilder();
    while (matcher.find()) {
      strictContent.append(matcher.group(1)).append("\n");
    }

    return strictContent.toString().trim();
  }

  /**
   * Парсит строгий формат
   */
  private List<Week> parseStrictFormat(String content) {
    List<Week> weeks = new ArrayList<>();

    // Разделяем на недели
    String[] weekBlocks = content.split("WEEK_START");

    for (String weekBlock : weekBlocks) {
      if (weekBlock.trim().isEmpty()) continue;

      try {
        Week week = parseWeekBlock(weekBlock);
        if (week != null) {
          weeks.add(week);
        }
      } catch (Exception e) {
        System.out.println("❌ Ошибка парсинга блока недели: " + e.getMessage());
      }
    }

    return weeks;
  }

  /**
   * Парсит блок одной недели
   */
  private Week parseWeekBlock(String weekBlock) {
    Week week = new Week();
    List<Task> tasks = new ArrayList<>();

    // Извлекаем номер недели
    Pattern numberPattern = Pattern.compile("NUMBER:(\\d+)");
    Matcher numberMatcher = numberPattern.matcher(weekBlock);
    if (!numberMatcher.find()) {
      System.out.println("❌ Не найден номер недели");
      return null;
    }

    int weekNumber = Integer.parseInt(numberMatcher.group(1));
    week.setNumber(weekNumber);

    // Извлекаем цель
    Pattern goalPattern = Pattern.compile("GOAL:([^\n]+)");
    Matcher goalMatcher = goalPattern.matcher(weekBlock);
    if (goalMatcher.find()) {
      week.setGoal(goalMatcher.group(1).trim());
    } else {
      week.setGoal("Неделя " + weekNumber);
    }

    // Извлекаем задачи
    Pattern taskPattern = Pattern.compile("TASK_START(.*?)TASK_END", Pattern.DOTALL);
    Matcher taskMatcher = taskPattern.matcher(weekBlock);

    while (taskMatcher.find()) {
      Task task = parseTaskBlock(taskMatcher.group(1));
      if (task != null) {
        tasks.add(task);
      }
    }

    // Гарантируем минимум 1 задачу
    if (tasks.isEmpty()) {
      tasks.add((Task) createDefaultTask(weekNumber));
    }

    week.setTasks(tasks);
    System.out.println("✅ Распарсена неделя " + weekNumber + " с " + tasks.size() + " задачами");

    return week;
  }

  /**
   * Парсит блок задачи
   */
  private Task parseTaskBlock(String taskBlock) {
    Task task = new Task();

    // Извлекаем описание
    Pattern descPattern = Pattern.compile("DESCRIPTION:([^\n]+)");
    Matcher descMatcher = descPattern.matcher(taskBlock);
    if (descMatcher.find()) {
      task.setDescription(descMatcher.group(1).trim());
    } else {
      return null; // Если нет описания - пропускаем задачу
    }

    // Извлекаем URL
    Pattern urlPattern = Pattern.compile("URLS:([^\n]+)");
    Matcher urlMatcher = urlPattern.matcher(taskBlock);
    if (urlMatcher.find()) {
      String urlsString = urlMatcher.group(1).trim();
      task.setUrls(parseUrls(urlsString));
    } else {
      task.setUrls(new ArrayList<>());
    }

    return task;
  }

  /**
   * Парсит URL из строки
   */
  private List<String> parseUrls(String urlsString) {
    List<String> urls = new ArrayList<>();
    if (urlsString == null || urlsString.trim().isEmpty()) {
      return urls;
    }

    // Разделяем по запятой
    String[] urlArray = urlsString.split(",");
    for (String url : urlArray) {
      String cleanUrl = url.trim();
      if (cleanUrl.startsWith("http")) {
        urls.add(cleanUrl);
      }
    }

    return urls;
  }

  /**
   * Создает неделю по умолчанию
   */
  private Week createDefaultWeek(int weekNumber) {
    Week week = new Week();
    week.setNumber(weekNumber);
    week.setGoal("Неделя " + weekNumber);
    week.setTasks(createDefaultTask(weekNumber));
    return week;
  }

  private List<Task> createDefaultTask(int weekNumber) {
    Task task = new Task();
    task.setDescription("Практическое задание недели " + weekNumber);
    task.setUrls(List.of("https://example.com/week" + weekNumber));
    return List.of(task);
  }

  private List<Week> createFallbackWeeks() {
    System.out.println("🔄 Создание запасного плана");
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

    for (int i = 0; i < 8; i++) {
      Week week = new Week();
      week.setNumber(i + 1);
      week.setGoal(goals[i]);

      List<Task> tasks = new ArrayList<>();
      Task task1 = new Task();
      task1.setDescription("Теоретическое изучение материалов");
      task1.setUrls(List.of("https://example.com/week" + (i + 1) + "-theory"));

      Task task2 = new Task();
      task2.setDescription("Практическое задание");
      task2.setUrls(List.of("https://example.com/week" + (i + 1) + "-practice"));

      tasks.add(task1);
      tasks.add(task2);
      week.setTasks(tasks);
      weeks.add(week);
    }

    return weeks;
  }
}