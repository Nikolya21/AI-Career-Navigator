package com.aicareer.app.servlets;

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

@WebServlet("/career-roadmap")
public class CareerRoadmapServlet extends HttpServlet {

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

    // Проверяем, завершен ли диалог
    Boolean discussionCompleted = (Boolean) session.getAttribute("vacancyDiscussionCompleted");
    if (discussionCompleted == null || !discussionCompleted) {
      System.out.println("⚠️ Диалог не завершен, перенаправляем к обсуждению");
      response.sendRedirect(request.getContextPath() + "/vacancy-discussion");
      return;
    }

    try {
      String selectedVacancy = (String) session.getAttribute("selectedVacancyName");
      String personalizedPlan = (String) session.getAttribute("personalizedVacancyPlan");

      // Получаем roadmap из сессии (должен быть сгенерирован в VacancyDiscussionServlet)
      Roadmap roadmap = (Roadmap) session.getAttribute("generatedRoadmap");

      if (roadmap == null) {
        System.out.println("⚠️ Roadmap не найден в сессии, создаем базовый...");
        // Создаем простой roadmap только если в сессии нет
        roadmap = createSimpleRoadmap(selectedVacancy, session);
        session.setAttribute("generatedRoadmap", roadmap);
      }

      // Передаем данные в JSP
      request.setAttribute("roadmap", roadmap);
      request.setAttribute("selectedVacancy", selectedVacancy);
      request.setAttribute("personalizedPlan", personalizedPlan);

      request.getRequestDispatcher("/jsp/CareerRoadmap.jsp").forward(request, response);

    } catch (Exception e) {
      System.err.println("❌ Ошибка при загрузке roadmap: " + e.getMessage());
      e.printStackTrace();

      // Создаем fallback roadmap при ошибке
      Roadmap fallbackRoadmap = createSimpleRoadmap(
          (String) session.getAttribute("selectedVacancyName"),
          session
      );
      request.setAttribute("roadmap", fallbackRoadmap);
      request.setAttribute("error", "Временные технические работы. Roadmap будет улучшен в ближайшее время.");
      request.getRequestDispatcher("/jsp/CareerRoadmap.jsp").forward(request, response);
    }
  }

  /**
   * Создание простого roadmap (используется только как fallback)
   */
  private Roadmap createSimpleRoadmap(String vacancy, HttpSession session) {
    Roadmap roadmap = new Roadmap();
    List<RoadmapZone> zones = new ArrayList<>();

    // Простая структура из 3 зон
    String[] zoneNames = {"Основы", "Практика", "Проекты"};

    for (int i = 0; i < zoneNames.length; i++) {
      RoadmapZone zone = new RoadmapZone();
      zone.setName(zoneNames[i] + " для " + vacancy);
      zone.setZoneOrder(i + 1);
      zone.setLearningGoal("Этап " + (i + 1) + " развития навыков");
      zone.setComplexityLevel(i == 0 ? "Начальный" : i == 1 ? "Средний" : "Продвинутый");
      zone.setWeeks(createWeeks(i * 2 + 1, i * 2 + 2, "Обучение"));
      zone.updateTimestamps();
      zones.add(zone);
    }

    roadmap.setRoadmapZones(zones);

    Long userId = (Long) session.getAttribute("userId");
    roadmap.setUserId(userId != null ? userId : 1L);
    roadmap.updateTimestamps();

    System.out.println("✅ Простой roadmap создан");
    return roadmap;
  }

  /**
   * Создание недель для зоны
   */
  private List<Week> createWeeks(int startWeek, int endWeek, String goalPrefix) {
    List<Week> weeks = new ArrayList<>();
    for (int i = startWeek; i <= endWeek; i++) {
      Week week = new Week();
      week.setNumber(i);
      week.setGoal(goalPrefix + " - неделя " + i);
      week.setTasks(createBasicTasks());
      week.updateTimestamps();
      weeks.add(week);
    }
    return weeks;
  }

  /**
   * Создание базовых задач
   */
  private List<Task> createBasicTasks() {
    List<Task> tasks = new ArrayList<>();

    Task task1 = new Task();
    task1.setDescription("Изучение теоретического материала");
    task1.updateTimestamps();
    tasks.add(task1);

    Task task2 = new Task();
    task2.setDescription("Практическое упражнение");
    task2.updateTimestamps();
    tasks.add(task2);

    Task task3 = new Task();
    task3.setDescription("Мини-проект для закрепления знаний");
    task3.updateTimestamps();
    tasks.add(task3);

    return tasks;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }
}