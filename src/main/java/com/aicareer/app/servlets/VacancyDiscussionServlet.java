package com.aicareer.app.servlets;

import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import com.aicareer.core.dto.courseDto.ResponseByWeek;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.courseModel.Task;
import com.fasterxml.jackson.databind.DatabindContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/vacancy-discussion")
public class VacancyDiscussionServlet extends HttpServlet {

  private GigaChatService gigaChatService;
  private RoadmapGenerateService roadmapGenerateService;

  @Override
  public void init() throws ServletException {
    super.init();
    this.gigaChatService = new GigaChatService();
    this.roadmapGenerateService = new RoadmapGenerateService(gigaChatService);
  }

  // ... остальные методы doGet и doPost остаются без изменений ...

  private void completeDiscussion(HttpSession session, List<String> history, String vacancy) {
    try {
      // Сохраняем промпт диалога
      String fullDiscussionPrompt = buildDiscussionPrompt(history, vacancy);
      session.setAttribute("fullDiscussionPrompt", fullDiscussionPrompt);

      // Генерируем персонализированный план
      String personalizedPlan = generatePersonalizedPlan(history, vacancy);
      session.setAttribute("personalizedVacancyPlan", personalizedPlan);

      // ✅ ДОБАВЛЯЕМ: Генерация полноценного roadmap
      Roadmap detailedRoadmap = generateDetailedRoadmapFromDiscussion(vacancy, history, personalizedPlan);
      session.setAttribute("generatedRoadmap", detailedRoadmap);

      // Помечаем диалог как завершенный
      session.setAttribute("vacancyDiscussionCompleted", true);

      System.out.println("✅ Диалог завершен. Roadmap сгенерирован: " +
          detailedRoadmap.getRoadmapZones().size() + " зон");

    } catch (Exception e) {
      System.err.println("❌ Ошибка при завершении диалога: " + e.getMessage());
      e.printStackTrace();
      // Даже при ошибке помечаем диалог как завершенный
      session.setAttribute("vacancyDiscussionCompleted", true);
    }
  }

  /**
   * Генерация детального roadmap на основе диалога
   */
  private Roadmap generateDetailedRoadmapFromDiscussion(String vacancy, List<String> history, String personalizedPlan) {
    try {
      System.out.println("🎯 Генерация детального roadmap для: " + vacancy);

      // Создаем недели на основе диалога
      List<Week> weeks = generateWeeksFromDiscussion(vacancy, history, personalizedPlan);

      // Создаем ResponseByWeek для RoadmapGenerateService
      ResponseByWeek responseByWeek = new ResponseByWeek(weeks);

      // Используем RoadmapGenerateService для создания структуры зон
      String weeksInfo = roadmapGenerateService.gettingWeeksInformation(responseByWeek);
      String zonesAnalysis = roadmapGenerateService.informationComplexityAndQuantityAnalyzeAndCreatingZone(weeksInfo);
      List<RoadmapZone> zones = roadmapGenerateService.splittingWeeksIntoZones(zonesAnalysis, weeks);

      // Создаем финальный roadmap
      Roadmap roadmap = roadmapGenerateService.identifyingThematicallySimilarZones(zones);

      // Устанавливаем пользователя и временные метки

      Long userId = (Long) session.getAttribute("userId");
      roadmap.setUserId(userId != null ? userId : 1L);
      roadmap.updateTimestamps();

      System.out.println("✅ Детальный roadmap создан: " + zones.size() + " зон, " + weeks.size() + " недель");
      return roadmap;

    } catch (Exception e) {
      System.err.println("❌ Ошибка при генерации детального roadmap: " + e.getMessage());
      e.printStackTrace();
      // Возвращаем fallback roadmap при ошибке
      return createFallbackRoadmap(vacancy, history, personalizedPlan);
    }
  }

  /**
   * Генерация недель на основе диалога
   */
  private List<Week> generateWeeksFromDiscussion(String vacancy, List<String> history, String personalizedPlan) {
    try {
      // Собираем контекст диалога для генерации недель
      String discussionContext = buildDiscussionContextForWeeks(history, vacancy, personalizedPlan);

      // Генерируем структуру недель через GigaChat
      String weeksPrompt = createWeeksGenerationPrompt(discussionContext, vacancy);
      String weeksResponse = gigaChatService.sendMessage(weeksPrompt);

      // Парсим ответ и создаем недели
      return parseWeeksFromResponse(weeksResponse, vacancy);

    } catch (Exception e) {
      System.err.println("❌ Ошибка при генерации недель: " + e.getMessage());
      return createDefaultWeeks(vacancy);
    }
  }

  /**
   * Создание промпта для генерации недель
   */
  private String createWeeksGenerationPrompt(String discussionContext, String vacancy) {
    return "На основе следующего диалога о вакансии " + vacancy +
        " создай план обучения из 8 недель. Каждая неделя должна иметь:\n" +
        "1. Номер недели (от 1 до 8)\n" +
        "2. Конкретную цель на неделю\n" +
        "3. 2-3 практические задачи\n" +
        "4. Релевантные ссылки на ресурсы\n\n" +
        "Контекст диалога:\n" + discussionContext + "\n\n" +
        "Формат ответа для каждой недели:\n" +
        "WEEK:[номер]\n" +
        "GOAL:[цель недели]\n" +
        "TASKS:[задача 1];[задача 2];[задача 3]\n" +
        "URLS:[url1];[url2]\n" +
        "---\n" +
        "Начни сразу с WEEK:1";
  }

  /**
   * Парсинг недель из ответа AI
   */
  private List<Week> parseWeeksFromResponse(String response, String vacancy) {
    List<Week> weeks = new ArrayList<>();
    String[] weekBlocks = response.split("---");

    for (String block : weekBlocks) {
      if (block.trim().isEmpty()) continue;

      try {
        Week week = new Week();
        String[] lines = block.trim().split("\n");

        for (String line : lines) {
          if (line.startsWith("WEEK:")) {
            week.setNumber(Integer.parseInt(line.substring(5).trim()));
          } else if (line.startsWith("GOAL:")) {
            week.setGoal(line.substring(5).trim());
          } else if (line.startsWith("TASKS:")) {
            String tasksStr = line.substring(6).trim();
            String[] taskDescriptions = tasksStr.split(";");
            week.setTasks(createTasksFromDescriptions(taskDescriptions));
          }
        }

        week.updateTimestamps();
        weeks.add(week);

      } catch (Exception e) {
        System.err.println("❌ Ошибка парсинга недели: " + e.getMessage());
      }
    }

    // Если не удалось распарсить, создаем недели по умолчанию
    if (weeks.isEmpty()) {
      return createDefaultWeeks(vacancy);
    }

    return weeks;
  }

  /**
   * Создание задач из описаний
   */
  private List<Task> createTasksFromDescriptions(String[] descriptions) {
    List<Task> tasks = new ArrayList<>();
    for (String desc : descriptions) {
      if (desc.trim().isEmpty()) continue;

      Task task = new Task();
      task.setDescription(desc.trim());
      task.setUrls(getRelevantUrlsForTask(desc.trim()));
      task.updateTimestamps();
      tasks.add(task);
    }
    return tasks;
  }

  /**
   * Получение релевантных URL для задачи
   */
  private List<String> getRelevantUrlsForTask(String taskDescription) {
    List<String> urls = new ArrayList<>();

    // Базовые URL для разных типов задач
    if (taskDescription.toLowerCase().contains("java") || taskDescription.toLowerCase().contains("программир")) {
      urls.add("https://habr.com/ru/hub/java/");
      urls.add("https://javarush.com/");
    }
    if (taskDescription.toLowerCase().contains("spring")) {
      urls.add("https://spring.io/guides");
      urls.add("https://www.baeldung.com/spring-tutorial");
    }
    if (taskDescription.toLowerCase().contains("sql") || taskDescription.toLowerCase().contains("баз")) {
      urls.add("https://www.w3schools.com/sql/");
      urls.add("https://sql-academy.org/");
    }
    if (taskDescription.toLowerCase().contains("алгоритм")) {
      urls.add("https://leetcode.com/");
      urls.add("https://habr.com/ru/hub/algorithms/");
    }

    // Добавляем общие ресурсы если специфических не найдено
    if (urls.isEmpty()) {
      urls.add("https://habr.com/ru/");
      urls.add("https://stepik.org/");
    }

    return urls;
  }

  /**
   * Создание недель по умолчанию
   */
  private List<Week> createDefaultWeeks(String vacancy) {
    List<Week> weeks = new ArrayList<>();
    for (int i = 1; i <= 8; i++) {
      Week week = new Week();
      week.setNumber(i);
      week.setGoal("Изучение " + vacancy + " - неделя " + i);
      week.setTasks(createDefaultTasks());
      week.updateTimestamps();
      weeks.add(week);
    }
    return weeks;
  }

  /**
   * Создание задач по умолчанию
   */
  private List<Task> createDefaultTasks() {
    List<Task> tasks = new ArrayList<>();

    Task task1 = new Task();
    task1.setDescription("Изучение теоретического материала");
    task1.setUrls(List.of("https://habr.com/ru/", "https://stepik.org/"));
    tasks.add(task1);

    Task task2 = new Task();
    task2.setDescription("Практическое упражнение");
    task2.setUrls(List.of("https://leetcode.com/", "https://codewars.com/"));
    tasks.add(task2);

    Task task3 = new Task();
    task3.setDescription("Мини-проект для закрепления знаний");
    task3.setUrls(List.of("https://github.com/", "https://glitch.com/"));
    tasks.add(task3);

    return tasks;
  }

  /**
   * Fallback roadmap при ошибках
   */
  private Roadmap createFallbackRoadmap(String vacancy, List<String> history, String personalizedPlan) {
    System.out.println("🔄 Создание fallback roadmap для: " + vacancy);

    Roadmap roadmap = new Roadmap();
    List<RoadmapZone> zones = new ArrayList<>();

    // Анализируем диалог для персонализации fallback
    boolean hasExperience = history.stream()
        .anyMatch(msg -> msg.toLowerCase().contains("опыт") && !msg.toLowerCase().contains("нет опыта"));

    // Создаем зоны на основе анализа диалога
    if (!hasExperience) {
      RoadmapZone foundationZone = createRoadmapZone("Основы и введение", 1, "Начальный",
          "Изучение фундаментальных концепций " + vacancy, 1, 3);
      zones.add(foundationZone);
    }

    RoadmapZone practiceZone = createRoadmapZone("Практика и применение", 2, "Средний",
        "Разработка практических навыков для " + vacancy,
        zones.isEmpty() ? 1 : 4,
        zones.isEmpty() ? 6 : 7);
    zones.add(practiceZone);

    RoadmapZone projectsZone = createRoadmapZone("Проекты и портфолио", 3, "Продвинутый",
        "Создание проектов для портфолио",
        practiceZone.getWeeks().get(practiceZone.getWeeks().size()-1).getNumber() + 1,
        practiceZone.getWeeks().get(practiceZone.getWeeks().size()-1).getNumber() + 2);
    zones.add(projectsZone);

    roadmap.setRoadmapZones(zones);
    roadmap.setUserId(1L);
    roadmap.updateTimestamps();

    return roadmap;
  }

  /**
   * Создание зоны roadmap
   */
  private RoadmapZone createRoadmapZone(String name, int order, String complexity, String goal, int startWeek, int endWeek) {
    RoadmapZone zone = new RoadmapZone();
    zone.setName(name);
    zone.setZoneOrder(order);
    zone.setComplexityLevel(complexity);
    zone.setLearningGoal(goal);
    zone.setWeeks(createWeeksForZone(startWeek, endWeek, name));
    zone.updateTimestamps();
    return zone;
  }

  /**
   * Создание недель для зоны
   */
  private List<Week> createWeeksForZone(int startWeek, int endWeek, String zoneName) {
    List<Week> weeks = new ArrayList<>();
    for (int i = startWeek; i <= endWeek; i++) {
      Week week = new Week();
      week.setNumber(i);
      week.setGoal(zoneName + " - неделя " + i);
      week.setTasks(createDefaultTasks());
      week.updateTimestamps();
      weeks.add(week);
    }
    return weeks;
  }

  /**
   * Построение контекста для генерации недель
   */
  private String buildDiscussionContextForWeeks(List<String> history, String vacancy, String personalizedPlan) {
    StringBuilder context = new StringBuilder();
    context.append("Вакансия: ").append(vacancy).append("\n\n");
    context.append("Персонализированный план: ").append(personalizedPlan).append("\n\n");
    context.append("Ключевые моменты диалога:\n");

    // Берем только ответы пользователя для анализа
    for (int i = 1; i < history.size(); i += 2) {
      if (i < history.size()) {
        context.append("- ").append(history.get(i)).append("\n");
      }
    }

    return context.toString();
  }

  // ... остальные методы (buildDiscussionPrompt, generatePersonalizedPlan, setupDiscussionPage) остаются без изменений ...
}