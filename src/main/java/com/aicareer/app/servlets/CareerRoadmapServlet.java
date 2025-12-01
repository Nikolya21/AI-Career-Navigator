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

@WebServlet("/career-roadmap")
public class CareerRoadmapServlet extends HttpServlet {

  private GigaChatService gigaChatService;
  private LearningPlanAssembler learningPlanAssembler;
  private RoadmapGenerateService roadmapGenerateService;

  @Override
  public void init() throws ServletException {
    super.init();
    this.gigaChatService = new GigaChatService();
    initializeRoadmapServices();
  }

  private void initializeRoadmapServices() {
    // Сервисы для генерации курса
    ServicePrompt servicePrompt = new ServicePrompt();
    ServiceGenerateCourse courseGenerator = new ServiceGenerateCourse(servicePrompt, gigaChatService);
    ServiceWeek courseResponse = new ServiceWeek();
    WeekDistributionService distributionService = new WeekDistributionService();

    // Ассемблер учебного плана
    this.learningPlanAssembler = new LearningPlanAssembler(courseGenerator, courseResponse, distributionService);

    // Сервис генерации roadmap
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

    try {
      String selectedVacancy = (String) session.getAttribute("selectedVacancyName");
      String personalizedPlan = (String) session.getAttribute("personalizedVacancyPlan");
      String fullDiscussionPrompt = (String) session.getAttribute("fullDiscussionPrompt");

      // ✅ ПОЛУЧАЕМ РЕАЛЬНЫЙ ROADMAP ИЗ СЕССИИ (сгенерированный в VacancyDiscussionServlet)
      Roadmap roadmap = (Roadmap) session.getAttribute("generatedRoadmap");

      if (roadmap == null) {
        // ✅ ЕСЛИ ROADMAP НЕТ В СЕССИИ - ГЕНЕРИРУЕМ ЕГО ЗДЕСЬ НА ОСНОВЕ ПРОМПТА
        System.out.println("🔄 Roadmap не найден в сессии, запускаем генерацию...");
        roadmap = generateRoadmapFromDiscussion(session, selectedVacancy, fullDiscussionPrompt, personalizedPlan);

        if (roadmap != null) {
          session.setAttribute("generatedRoadmap", roadmap);
          System.out.println("✅ Roadmap успешно сгенерирован в CareerRoadmapServlet");
        }
      }

      if (roadmap == null) {
        // Если roadmap все еще не сгенерирован, перенаправляем обратно к обсуждению вакансии
        System.err.println("❌ Не удалось сгенерировать roadmap");
        response.sendRedirect(request.getContextPath() + "/vacancy-discussion");
        return;
      }

      // Передаем реальные данные в JSP
      request.setAttribute("roadmap", roadmap);
      request.setAttribute("selectedVacancy", selectedVacancy);
      request.setAttribute("personalizedPlan", personalizedPlan);

      // ✅ ПЕРЕДАЕМ ПРОМПТ ДЛЯ ОТОБРАЖЕНИЯ В JSP (если нужно)
      request.setAttribute("hasDiscussionData", fullDiscussionPrompt != null && !fullDiscussionPrompt.isEmpty());

      request.getRequestDispatcher("/jsp/CareerRoadmap.jsp").forward(request, response);

    } catch (Exception e) {
      System.err.println("❌ Ошибка при загрузке roadmap: " + e.getMessage());
      e.printStackTrace();
      request.setAttribute("error", "Временные технические работы. Roadmap будет доступен в ближайшее время.");
      request.getRequestDispatcher("/jsp/CareerRoadmap.jsp").forward(request, response);
    }
  }

  /**
   * ✅ МЕТОД ДЛЯ ГЕНЕРАЦИИ ROADMAP НА ОСНОВЕ ПРОМПТА ИЗ ДИАЛОГА
   */
  private Roadmap generateRoadmapFromDiscussion(HttpSession session, String vacancy, String discussionPrompt, String personalizedPlan) {
    try {
      System.out.println("🚀 Запуск генерации roadmap на основе диалога для вакансии: " + vacancy);

      if (discussionPrompt == null || discussionPrompt.trim().isEmpty()) {
        System.err.println("❌ Промпт диалога пустой, невозможно сгенерировать roadmap");
        return null;
      }

      System.out.println("📝 Используем промпт из диалога (" + discussionPrompt.length() + " символов)");

      // 1. СОЗДАЕМ ТРЕБОВАНИЯ ДЛЯ КУРСА НА ОСНОВЕ ПРОМПТА ИЗ ДИАЛОГА
      CourseRequest courseRequest = createPersonalizedCourseRequest(vacancy, discussionPrompt, personalizedPlan);

      // 2. Генерируем учебный план (8 недель)
      ResponseByWeek responseByWeek = learningPlanAssembler.assemblePlan(courseRequest);
      System.out.println("✅ Учебный план сгенерирован: " + responseByWeek.getWeeks().size() + " недель");

      // 3. Получаем информацию о неделях в текстовом формате
      String weeksInformation = roadmapGenerateService.gettingWeeksInformation(responseByWeek);

      // 4. Анализируем сложность и создаем зоны
      String zonesAnalysis = roadmapGenerateService.informationComplexityAndQuantityAnalyzeAndCreatingZone(weeksInformation);

      // 5. Разбиваем недели на зоны
      List<RoadmapZone> zones = roadmapGenerateService.splittingWeeksIntoZones(
          zonesAnalysis, responseByWeek.getWeeks());

      // 6. Создаем финальный roadmap
      Roadmap roadmap = roadmapGenerateService.identifyingThematicallySimilarZones(zones);

      // 7. Устанавливаем пользователя
      Long userId = (Long) session.getAttribute("userId");
      if (userId != null) {
        roadmap.setUserId(userId);
      } else {
        roadmap.setUserId(1L); // fallback
      }

      System.out.println("🎉 Roadmap успешно сгенерирован в CareerRoadmapServlet: " +
          roadmap.getRoadmapZones().size() + " зон, " +
          calculateTotalWeeks(roadmap) + " недель");

      return roadmap;

    } catch (Exception e) {
      System.err.println("❌ Ошибка при генерации roadmap в CareerRoadmapServlet: " + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  /**
   * ✅ СОЗДАЕТ ПЕРСОНАЛИЗИРОВАННЫЕ ТРЕБОВАНИЯ КУРСА НА ОСНОВЕ ДИАЛОГА
   */
  private CourseRequest createPersonalizedCourseRequest(String vacancy, String discussionPrompt, String personalizedPlan) {
    CourseRequest request = new CourseRequest();

    // ✅ КОМБИНИРУЕМ ВАКАНСИЮ, ПРОМПТ ИЗ ДИАЛОГА И ПЕРСОНАЛИЗИРОВАННЫЙ ПЛАН
    String courseRequirements = String.format(
        "ПЕРСОНАЛИЗИРОВАННЫЕ ТРЕБОВАНИЯ ДЛЯ СОЗДАНИЯ КУРСА\n\n" +
            "ЦЕЛЕВАЯ ВАКАНСИЯ: %s\n\n" +
            "ИСТОРИЯ ДИАЛОГА С ПОЛЬЗОВАТЕЛЕМ:\n" +
            "%s\n\n" +
            "ПЕРСОНАЛИЗИРОВАННЫЙ ПЛАН РАЗВИТИЯ:\n" +
            "%s\n\n" +
            "ИНСТРУКЦИЯ ДЛЯ ГЕНЕРАЦИИ КУРСА:\n" +
            "На основе полного диалога с пользователем создай детализированный учебный план, который:\n" +
            "1. Учитывает текущий уровень знаний и опыт пользователя (определенный из диалога)\n" +
            "2. Фокусируется на конкретных навыках, необходимых для вакансии '%s'\n" +
            "3. Учитывает карьерные цели, предпочтения и ограничения пользователя\n" +
            "4. Предоставляет практические задания, проекты и реальные кейсы\n" +
            "5. Включает актуальные ресурсы для самостоятельного изучения\n" +
            "6. Адаптирован под темп обучения и доступное время пользователя\n\n" +
            "Структура курса должна быть логичной и последовательной, от основ к продвинутым темам.",
        vacancy,
        discussionPrompt.length() > 3000 ? discussionPrompt.substring(0, 3000) + "..." : discussionPrompt,
        personalizedPlan != null ? personalizedPlan : "План будет определен на основе диалога",
        vacancy
    );

    request.setCourseRequirements(courseRequirements);

    System.out.println("📋 Созданы персонализированные требования курса на основе диалога");
    return request;
  }

  /**
   * ✅ ВСПОМОГАТЕЛЬНЫЙ МЕТОД ДЛЯ ПОДСЧЕТА НЕДЕЛЬ
   */
  private int calculateTotalWeeks(Roadmap roadmap) {
    if (roadmap.getRoadmapZones() == null) return 0;
    int totalWeeks = 0;
    for (RoadmapZone zone : roadmap.getRoadmapZones()) {
      if (zone.getWeeks() != null) {
        totalWeeks += zone.getWeeks().size();
      }
    }
    return totalWeeks;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Обработка POST запросов (если понадобится)
    doGet(request, response);
  }
}
