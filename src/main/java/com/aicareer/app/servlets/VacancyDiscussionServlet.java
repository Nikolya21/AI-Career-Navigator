package com.aicareer.app.servlets;

import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import com.aicareer.core.dto.courseDto.ResponseByWeek;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.courseModel.Task;
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

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType("text/html; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    request.setCharacterEncoding("UTF-8");

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("authenticated") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    String selectedVacancy = (String) session.getAttribute("selectedVacancyName");
    if (selectedVacancy == null) {
      response.sendRedirect(request.getContextPath() + "/choose-vacancy");
      return;
    }

    // Проверяем, не завершен ли уже диалог
    Boolean discussionCompleted = (Boolean) session.getAttribute("vacancyDiscussionCompleted");
    if (discussionCompleted != null && discussionCompleted) {
      System.out.println("🔁 Диалог уже завершен, перенаправляем на roadmap");
      response.sendRedirect(request.getContextPath() + "/career-roadmap");
      return;
    }

    // Проверяем, есть ли уже активный диалог
    List<String> existingHistory = (List<String>) session.getAttribute("vacancyDiscussionHistory");
    Integer existingQuestionCount = (Integer) session.getAttribute("vacancyDiscussionCount");

    if (existingHistory != null && !existingHistory.isEmpty() && existingQuestionCount != null) {
      System.out.println("🔄 Продолжение существующего диалога. Вопросов: " + existingQuestionCount);
      setupDiscussionPage(request, session);
      request.getRequestDispatcher("/jsp/VacancyDiscussion.jsp").forward(request, response);
      return;
    }

    // Инициализируем новый диалог
    System.out.println("🆕 Инициализация нового диалога для вакансии: " + selectedVacancy);
    initializeVacancyDiscussion(session, selectedVacancy);
    setupDiscussionPage(request, session);
    request.getRequestDispatcher("/jsp/VacancyDiscussion.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType("text/html; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    request.setCharacterEncoding("UTF-8");

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("authenticated") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    Boolean discussionCompleted = (Boolean) session.getAttribute("vacancyDiscussionCompleted");
    if (discussionCompleted != null && discussionCompleted) {
      System.out.println("⚠️ Попытка отправить сообщение в завершенный диалог");
      response.sendRedirect(request.getContextPath() + "/career-roadmap");
      return;
    }

    String message = request.getParameter("message");
    String selectedVacancy = (String) session.getAttribute("selectedVacancyName");

    if (message != null && !message.trim().isEmpty()) {
      processUserResponse(session, selectedVacancy, message.trim(), request, response);
    } else {
      setupDiscussionPage(request, session);
      request.getRequestDispatcher("/jsp/VacancyDiscussion.jsp").forward(request, response);
    }
  }

  private void initializeVacancyDiscussion(HttpSession session, String vacancy) {
    List<String> discussionHistory = new ArrayList<>();
    String welcomeMessage = generateWelcomeMessage(vacancy);
    discussionHistory.add(welcomeMessage);

    session.setAttribute("vacancyDiscussionHistory", discussionHistory);
    session.setAttribute("vacancyDiscussionCount", 1); // Первый вопрос уже задан
    session.setAttribute("currentDiscussionType", "vacancy_discussion");
    session.setAttribute("vacancyDiscussionCompleted", false);

    System.out.println("🔍 Инициализирован диалог для вакансии: " + vacancy);
  }

  private String generateWelcomeMessage(String vacancy) {
    try {
      String prompt = "Пользователь выбрал вакансию: " + vacancy +
          ". Начни диалог для обсуждения этой вакансии. " +
          "Задай первый вопрос, который поможет понять:\n" +
          "1. Почему пользователь выбрал именно эту вакансию\n" +
          "2. Какой у него текущий опыт в этой области\n" +
          "3. Какие навыки уже есть, а какие нужно развить\n" +
          "4. Какие карьерные цели\n\n" +
          "Вопрос должен быть конкретным и направляющим. Начни прямо с вопроса.";

      System.out.println("🤖 Генерация приветственного сообщения...");
      return gigaChatService.sendMessage(prompt);
    } catch (Exception e) {
      System.err.println("❌ Ошибка при генерации приветственного сообщения: " + e.getMessage());
      return "Здравствуйте! Вы выбрали вакансию " + vacancy + ". Расскажите, почему вас заинтересовало это направление?";
    }
  }

  private void processUserResponse(HttpSession session, String vacancy, String userMessage,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    List<String> discussionHistory = (List<String>) session.getAttribute("vacancyDiscussionHistory");
    Integer questionCount = (Integer) session.getAttribute("vacancyDiscussionCount");

    if (discussionHistory == null) {
      discussionHistory = new ArrayList<>();
    }
    if (questionCount == null) {
      questionCount = 1;
    }

    System.out.println("📊 Обработка ответа. Текущий счетчик: " + questionCount + "/5");

    // Добавляем ответ пользователя в историю
    discussionHistory.add(userMessage);
    System.out.println("✅ Ответ пользователя добавлен в историю");

    // Проверяем, нужно ли задавать следующий вопрос
    if (questionCount < 5) {
      // Генерируем следующий вопрос
      System.out.println("🤖 Генерация вопроса " + (questionCount + 1) + "...");
      String nextQuestion = generateNextQuestion(discussionHistory, vacancy, questionCount);
      discussionHistory.add(nextQuestion);

      // Увеличиваем счетчик вопросов
      int newQuestionCount = questionCount + 1;
      session.setAttribute("vacancyDiscussionCount", newQuestionCount);
      session.setAttribute("vacancyDiscussionHistory", discussionHistory);

      System.out.println("✅ Вопрос " + newQuestionCount + " сгенерирован и добавлен в историю");

      setupDiscussionPage(request, session);
      request.getRequestDispatcher("/jsp/VacancyDiscussion.jsp").forward(request, response);
    } else {
      // Это был 5-й ответ - завершаем диалог
      System.out.println("🎯 Получен 5-й ответ - завершение диалога");

      // Сохраняем финальную историю
      session.setAttribute("vacancyDiscussionHistory", discussionHistory);

      // Завершаем диалог
      completeDiscussion(session, discussionHistory, vacancy);

      // Немедленно перенаправляем на страницу roadmap
      System.out.println("🔄 Перенаправление на career-roadmap");
      response.sendRedirect(request.getContextPath() + "/career-roadmap");
    }
  }

  private String generateNextQuestion(List<String> history, String vacancy, int currentQuestion) {
    try {
      StringBuilder context = new StringBuilder();
      context.append("Обсуждаем вакансию: ").append(vacancy).append("\n\n");

      // Берем последние 4 сообщения для контекста
      int startIndex = Math.max(0, history.size() - 4);
      for (int i = startIndex; i < history.size(); i++) {
        if (i % 2 == 0) {
          context.append("AI: ").append(history.get(i)).append("\n");
        } else {
          context.append("User: ").append(history.get(i)).append("\n");
        }
      }

      String prompt = context.toString() +
          "\nНа основе этого диалога задай следующий уточняющий вопрос (" +
          (currentQuestion + 1) + "/5) для составления персонализированного плана развития к вакансии " +
          vacancy + ". Вопрос должен углублять понимание конкретных потребностей пользователя.";

      return gigaChatService.sendMessage(prompt);
    } catch (Exception e) {
      System.err.println("❌ Ошибка при генерации вопроса: " + e.getMessage());
      return "Расскажите подробнее о вашем опыте в этой области?";
    }
  }

  private void completeDiscussion(HttpSession session, List<String> history, String vacancy) {
    try {
      // Сохраняем промпт диалога
      String fullDiscussionPrompt = buildDiscussionPrompt(history, vacancy);
      session.setAttribute("fullDiscussionPrompt", fullDiscussionPrompt);

      // Генерируем персонализированный план
      String personalizedPlan = generatePersonalizedPlan(history, vacancy);
      session.setAttribute("personalizedVacancyPlan", personalizedPlan);

      // ✅ Генерация полноценного roadmap
      Roadmap detailedRoadmap = generateDetailedRoadmapFromDiscussion(vacancy, history, personalizedPlan, session);
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
  private Roadmap generateDetailedRoadmapFromDiscussion(String vacancy, List<String> history,
      String personalizedPlan, HttpSession session) {
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
      return createFallbackRoadmap(vacancy, history, personalizedPlan, session);
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
    task1.updateTimestamps();
    tasks.add(task1);

    Task task2 = new Task();
    task2.setDescription("Практическое упражнение");
    task2.setUrls(List.of("https://leetcode.com/", "https://codewars.com/"));
    task2.updateTimestamps();
    tasks.add(task2);

    Task task3 = new Task();
    task3.setDescription("Мини-проект для закрепления знаний");
    task3.setUrls(List.of("https://github.com/", "https://glitch.com/"));
    task3.updateTimestamps();
    tasks.add(task3);

    return tasks;
  }

  /**
   * Fallback roadmap при ошибках
   */
  private Roadmap createFallbackRoadmap(String vacancy, List<String> history, String personalizedPlan, HttpSession session) {
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

    Long userId = (Long) session.getAttribute("userId");
    roadmap.setUserId(userId != null ? userId : 1L);
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

  private String buildDiscussionPrompt(List<String> history, String vacancy) {
    StringBuilder fullPrompt = new StringBuilder();
    fullPrompt.append("ПОЛНАЯ ИСТОРИЯ ДИАЛОГА ДЛЯ ВАКАНСИИ: ").append(vacancy).append("\n\n");

    for (int i = 0; i < history.size(); i++) {
      if (i % 2 == 0) {
        fullPrompt.append("AI (вопрос ").append((i/2) + 1).append("): ").append(history.get(i)).append("\n");
      } else {
        fullPrompt.append("USER (ответ ").append((i/2) + 1).append("): ").append(history.get(i)).append("\n");
      }
      fullPrompt.append("---\n");
    }

    String result = fullPrompt.toString();
    System.out.println("📄 Сгенерирован промпт диалога (" + result.length() + " символов)");
    return result;
  }

  private String generatePersonalizedPlan(List<String> history, String vacancy) {
    try {
      StringBuilder fullDialog = new StringBuilder();
      fullDialog.append("Диалог об вакансии: ").append(vacancy).append("\n\n");

      for (int i = 0; i < history.size(); i++) {
        if (i % 2 == 0) {
          fullDialog.append("AI: ").append(history.get(i)).append("\n");
        } else {
          fullDialog.append("User: ").append(history.get(i)).append("\n");
        }
      }

      String prompt = "На основе этого диалога создай краткий персонализированный план развития для пользователя.\n" +
          "Вакансия: " + vacancy + "\n" +
          "Полный диалог:\n" + fullDialog.toString() + "\n\n" +
          "Создай структурированный план с этапами развития.";

      System.out.println("🤖 Генерация персонализированного плана...");
      return gigaChatService.sendMessage(prompt);
    } catch (Exception e) {
      System.err.println("❌ Ошибка при генерации плана: " + e.getMessage());
      return "Персонализированный план будет создан на основе ваших ответов.";
    }
  }

  private void setupDiscussionPage(HttpServletRequest request, HttpSession session) {
    List<String> discussionHistory = (List<String>) session.getAttribute("vacancyDiscussionHistory");
    if (discussionHistory != null) {
      request.setAttribute("discussionHistory", discussionHistory);
    }

    Integer questionCount = (Integer) session.getAttribute("vacancyDiscussionCount");
    if (questionCount != null) {
      request.setAttribute("questionsCount", questionCount);
    }

    String selectedVacancy = (String) session.getAttribute("selectedVacancyName");
    if (selectedVacancy != null) {
      request.setAttribute("selectedVacancy", selectedVacancy);
    }

    Boolean discussionCompleted = (Boolean) session.getAttribute("vacancyDiscussionCompleted");
    if (discussionCompleted != null && discussionCompleted) {
      request.setAttribute("showRoadmapButton", true);
    }
  }
}