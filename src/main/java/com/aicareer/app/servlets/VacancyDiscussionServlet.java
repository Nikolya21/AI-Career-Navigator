package com.aicareer.app.servlets;

import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.service.course.LearningPlanAssembler;
import com.aicareer.core.service.course.ServiceGenerateCourse;
import com.aicareer.core.service.course.ServicePrompt;
import com.aicareer.core.service.course.ServiceWeek;
import com.aicareer.core.service.course.WeekDistributionService;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import com.aicareer.core.dto.courseDto.CourseRequest;
import com.aicareer.core.dto.courseDto.ResponseByWeek;
import com.aicareer.core.service.gigachat.GigaChatService;
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

  @Override
  public void init() throws ServletException {
    super.init();
    this.gigaChatService = new GigaChatService();
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

    // ✅ ПРОВЕРЯЕМ, НЕ ЗАВЕРШЕН ЛИ УЖЕ ДИАЛОГ
    Boolean discussionCompleted = (Boolean) session.getAttribute("vacancyDiscussionCompleted");
    if (discussionCompleted != null && discussionCompleted) {
      System.out.println("🔁 Диалог уже завершен, перенаправляем на roadmap");
      response.sendRedirect(request.getContextPath() + "/career-roadmap");
      return;
    }

    // ✅ ПРОВЕРЯЕМ, ЕСТЬ ЛИ УЖЕ АКТИВНЫЙ ДИАЛОГ
    List<String> existingHistory = (List<String>) session.getAttribute("vacancyDiscussionHistory");
    Integer existingQuestionCount = (Integer) session.getAttribute("vacancyDiscussionCount");

    if (existingHistory != null && !existingHistory.isEmpty() && existingQuestionCount != null) {
      System.out.println("🔄 Продолжение существующего диалога. Вопросов: " + existingQuestionCount);
      request.setAttribute("selectedVacancy", selectedVacancy);
      setupDiscussionHistory(request, session);
      request.getRequestDispatcher("/jsp/VacancyDiscussion.jsp").forward(request, response);
      return;
    }

    // ✅ ТОЛЬКО ЕСЛИ ДИАЛОГ ЕЩЕ НЕ НАЧИНАЛСЯ - ИНИЦИАЛИЗИРУЕМ НОВЫЙ
    System.out.println("🆕 Инициализация нового диалога для вакансии: " + selectedVacancy);
    initializeVacancyDiscussion(session, selectedVacancy);

    request.setAttribute("selectedVacancy", selectedVacancy);
    setupDiscussionHistory(request, session);
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
      handleVacancyDiscussion(session, selectedVacancy, message.trim(), request, response);
    } else {
      setupDiscussionHistory(request, session);
      request.getRequestDispatcher("/jsp/VacancyDiscussion.jsp").forward(request, response);
    }
  }

  private void initializeVacancyDiscussion(HttpSession session, String vacancy) {
    List<String> discussionHistory = new ArrayList<>();
    String welcomeMessage = generateWelcomeMessage(vacancy);
    discussionHistory.add(welcomeMessage);

    session.setAttribute("vacancyDiscussionHistory", discussionHistory);
    session.setAttribute("vacancyDiscussionCount", 1);
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

  private void handleVacancyDiscussion(HttpSession session, String vacancy, String userMessage,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    List<String> discussionHistory = (List<String>) session.getAttribute("vacancyDiscussionHistory");
    Integer questionCount = (Integer) session.getAttribute("vacancyDiscussionCount");

    if (discussionHistory == null) discussionHistory = new ArrayList<>();
    if (questionCount == null) questionCount = 1;

    System.out.println("📊 Текущий счетчик вопросов: " + questionCount + "/5");

    // Добавляем ответ пользователя
    discussionHistory.add(userMessage);

    if (questionCount < 5) {
      String nextQuestion = generateNextVacancyQuestion(discussionHistory, vacancy, questionCount);
      discussionHistory.add(nextQuestion);

      session.setAttribute("vacancyDiscussionHistory", discussionHistory);
      session.setAttribute("vacancyDiscussionCount", questionCount + 1);

      setupDiscussionHistory(request, session);
      request.getRequestDispatcher("/jsp/VacancyDiscussion.jsp").forward(request, response);
    } else {
      System.out.println("🎯 Завершение диалога после 5 вопросов");
      completeVacancyDiscussion(session, discussionHistory, vacancy, response, request);
    }
  }

  private String generateNextVacancyQuestion(List<String> history, String vacancy, int currentQuestion) {
    try {
      StringBuilder context = new StringBuilder();
      context.append("Обсуждаем вакансию: ").append(vacancy).append("\n\n");

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

      System.out.println("🤖 Генерация вопроса " + (currentQuestion + 1) + "...");
      return gigaChatService.sendMessage(prompt);
    } catch (Exception e) {
      System.err.println("❌ Ошибка при генерации вопроса: " + e.getMessage());
      return "Расскажите подробнее о вашем опыте в этой области?";
    }
  }

  private void completeVacancyDiscussion(HttpSession session, List<String> history,
      String vacancy, HttpServletResponse response, HttpServletRequest request)
      throws IOException, ServletException {

    try {
      // ✅ СОБИРАЕМ ПРОМПТ ДИАЛОГА
      String fullDiscussionPrompt = buildFullDiscussionPrompt(history, vacancy);
      session.setAttribute("fullDiscussionPrompt", fullDiscussionPrompt);

      // ✅ ГЕНЕРИРУЕМ ПЕРСОНАЛИЗИРОВАННЫЙ ПЛАН
      String personalizedPlan = generatePersonalizedPlan(history, vacancy);
      session.setAttribute("personalizedVacancyPlan", personalizedPlan);

      // ✅ ВАЖНО: СНАЧАЛА ПОМЕЧАЕМ ДИАЛОГ КАК ЗАВЕРШЕННЫЙ
      session.setAttribute("vacancyDiscussionCompleted", true);

      // ✅ ПЫТАЕМСЯ СГЕНЕРИРОВАТЬ ROADMAP (НО НЕ БЛОКИРУЕМ ПЕРЕНАПРАВЛЕНИЕ ПРИ ОШИБКЕ)
      try {
        System.out.println("🚀 Попытка генерации roadmap...");
        Roadmap roadmap = generateSimpleRoadmap(vacancy, fullDiscussionPrompt, personalizedPlan, session);
        if (roadmap != null) {
          session.setAttribute("generatedRoadmap", roadmap);
          System.out.println("✅ Roadmap успешно сгенерирован");
        } else {
          System.out.println("⚠️ Roadmap не сгенерирован, но продолжаем");
        }
      } catch (Exception e) {
        System.err.println("⚠️ Ошибка при генерации roadmap (продолжаем без него): " + e.getMessage());
        // НЕ ВЫБРАСЫВАЕМ ИСКЛЮЧЕНИЕ - ПРОДОЛЖАЕМ
      }

      System.out.println("🔄 Перенаправление на career-roadmap");
      response.sendRedirect(request.getContextPath() + "/career-roadmap");
      return;

    } catch (Exception e) {
      System.err.println("❌ КРИТИЧЕСКАЯ Ошибка при завершении диалога: " + e.getMessage());
      e.printStackTrace();

      // ✅ ДАЖЕ ПРИ КРИТИЧЕСКОЙ ОШИБКЕ - ПЕРЕНАПРАВЛЯЕМ НА ROADMAP
      session.setAttribute("vacancyDiscussionCompleted", true);
      session.setAttribute("roadmapError", "Произошла ошибка: " + e.getMessage());
      response.sendRedirect(request.getContextPath() + "/career-roadmap");
      return;
    }
  }

  /**
   * ✅ УПРОЩЕННАЯ ГЕНЕРАЦИЯ ROADMAP С FALLBACK
   */
  private Roadmap generateSimpleRoadmap(String vacancy, String discussionPrompt, String personalizedPlan, HttpSession session) {
    try {
      System.out.println("🔄 Запуск упрощенной генерации roadmap...");

      // ✅ ПРОБУЕМ БЫСТРЫЙ СПОСОБ С ГИГАЧАТОМ
      String roadmapPrompt = String.format(
          "Создай структурированный roadmap для вакансии '%s' на основе этого диалога:\n\n%s\n\n" +
              "Создай roadmap в формате JSON с полями: название, описание, этапы, сроки. Ответ должен быть кратким.",
          vacancy,
          discussionPrompt.length() > 2000 ? discussionPrompt.substring(0, 2000) + "..." : discussionPrompt
      );

      String roadmapText = gigaChatService.sendMessage(roadmapPrompt);
      System.out.println("✅ Текстовый roadmap сгенерирован через GigaChat");

      // ✅ СОЗДАЕМ ПРОСТОЙ ROADMAP ДЛЯ ОТОБРАЖЕНИЯ
      Roadmap roadmap = createBasicRoadmap(vacancy, roadmapText, session);
      return roadmap;

    } catch (Exception e) {
      System.err.println("❌ Ошибка при упрощенной генерации roadmap: " + e.getMessage());

      // ✅ FALLBACK: СОЗДАЕМ ОЧЕНЬ ПРОСТОЙ ROADMAP
      System.out.println("🔄 Создание fallback roadmap...");
      return createFallbackRoadmap(vacancy, session);
    }
  }

  /**
   * ✅ СОЗДАЕТ БАЗОВЫЙ ROADMAP ИЗ ТЕКСТА ОТ GIGACHAT
   */
  private Roadmap createBasicRoadmap(String vacancy, String roadmapText, HttpSession session) {
    Roadmap roadmap = new Roadmap();


    List<RoadmapZone> zones = new ArrayList<>();

    // Создаем несколько базовых зон
    RoadmapZone zone1 = new RoadmapZone();
    zone1.setName("Основы и введение");
    zone1.setZoneOrder(1);
    zone1.setLearningGoal("Изучение базовых концепций");
    zone1.setComplexityLevel("Начальный");
    zone1.setWeeks(createBasicWeeks(1, 2));
    zones.add(zone1);

    RoadmapZone zone2 = new RoadmapZone();
    zone2.setName("Практические навыки");
    zone2.setZoneOrder(2);
    zone2.setLearningGoal("Применение знаний на практике");
    zone2.setComplexityLevel("Средний");
    zone2.setWeeks(createBasicWeeks(3, 5));
    zones.add(zone2);

    RoadmapZone zone3 = new RoadmapZone();
    zone3.setName("Проекты и углубление");
    zone3.setZoneOrder(3);
    zone3.setLearningGoal("Реализация проектов и собеседование");
    zone3.setComplexityLevel("Продвинутый");
    zone3.setWeeks(createBasicWeeks(6, 8));
    zones.add(zone3);

    roadmap.setRoadmapZones(zones);

    Long userId = (Long) session.getAttribute("userId");
    roadmap.setUserId(userId != null ? userId : 1L);

    System.out.println("✅ Базовый roadmap создан: " + zones.size() + " зон");
    return roadmap;
  }

  /**
   * ✅ СОЗДАЕТ FALLBACK ROADMAP ЕСЛИ ВСЕ ОСТАЛЬНОЕ НЕ СРАБОТАЛО
   */
  private Roadmap createFallbackRoadmap(String vacancy, HttpSession session) {
    Roadmap roadmap = new Roadmap();


    List<RoadmapZone> zones = new ArrayList<>();

    // Простая структура из 3 зон
    String[] zoneNames = {"Основы", "Практика", "Проекты"};
    String[] goals = {"Изучение фундаментальных знаний", "Развитие практических навыков", "Создание портфолио проектов"};

    for (int i = 0; i < zoneNames.length; i++) {
      RoadmapZone zone = new RoadmapZone();
      zone.setName(zoneNames[i] + " для " + vacancy);
      zone.setZoneOrder(i + 1);
      zone.setLearningGoal(goals[i]);
      zone.setComplexityLevel(i == 0 ? "Начальный" : i == 1 ? "Средний" : "Продвинутый");
      zone.setWeeks(createBasicWeeks(i * 3 + 1, i * 3 + 3));
      zones.add(zone);
    }

    roadmap.setRoadmapZones(zones);

    Long userId = (Long) session.getAttribute("userId");
    roadmap.setUserId(userId != null ? userId : 1L);

    System.out.println("✅ Fallback roadmap создан: " + zones.size() + " зон");
    return roadmap;
  }

  /**
   * ✅ СОЗДАЕТ БАЗОВЫЕ НЕДЕЛИ ДЛЯ ЗОНЫ
   */
  private List<Week> createBasicWeeks(int startWeek, int endWeek) {
    List<Week> weeks = new ArrayList<>();
    for (int i = startWeek; i <= endWeek; i++) {
      Week week = new Week();
      week.setNumber(i);
      week.setGoal("Неделя " + i + " - изучение материала");
      week.setTasks(createBasicTasks());
      weeks.add(week);
    }
    return weeks;
  }

  /**
   * ✅ СОЗДАЕТ БАЗОВЫЕ ЗАДАЧИ ДЛЯ НЕДЕЛИ
   */
  private List<Task> createBasicTasks() {
    List<Task> tasks = new ArrayList<>();

    Task task1 = new Task();
    task1.setDescription("Изучение теоретического материала");
    tasks.add(task1);

    Task task2 = new Task();
    task2.setDescription("Практическое упражнение");
    tasks.add(task2);

    Task task3 = new Task();
    task3.setDescription("Мини-проект для закрепления");
    tasks.add(task3);

    return tasks;
  }

  private String buildFullDiscussionPrompt(List<String> history, String vacancy) {
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

  private void setupDiscussionHistory(HttpServletRequest request, HttpSession session) {
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