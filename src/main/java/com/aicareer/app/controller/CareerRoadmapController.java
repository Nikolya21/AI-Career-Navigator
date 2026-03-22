package com.aicareer.app.controller;

import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.roadmap.RoadmapZone;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/career-roadmap")
public class CareerRoadmapController {

  @GetMapping
  public String showRoadmap(HttpSession session, Model model) {
    if (session.getAttribute("authenticated") == null) {
      return "redirect:/login";
    }

    Boolean discussionCompleted = (Boolean) session.getAttribute("vacancyDiscussionCompleted");
    if (discussionCompleted == null || !discussionCompleted) {
      log.info("⚠️ Диалог не завершен, перенаправляем к обсуждению");
      return "redirect:/vacancy-discussion";
    }

    try {
      String selectedVacancy = (String) session.getAttribute("selectedVacancyName");
      String personalizedPlan = (String) session.getAttribute("personalizedVacancyPlan");
      Roadmap roadmap = (Roadmap) session.getAttribute("generatedRoadmap");

      if (roadmap == null) {
        log.info("⚠️ Roadmap не найден в сессии, создаем базовый...");
        roadmap = createSimpleRoadmap(selectedVacancy, session);
        session.setAttribute("generatedRoadmap", roadmap);
      }

      String safePersonalizedPlan = escapeHtmlForMarkdown(personalizedPlan);
      session.setAttribute("personalizedVacancyPlan", safePersonalizedPlan);

      model.addAttribute("roadmap", roadmap);
      model.addAttribute("selectedVacancy", selectedVacancy);

      return "CareerRoadmap";

    } catch (Exception e) {
      log.error("❌ Ошибка при загрузке roadmap", e);

      Roadmap fallbackRoadmap = createSimpleRoadmap(
          (String) session.getAttribute("selectedVacancyName"),
          session
      );
      model.addAttribute("roadmap", fallbackRoadmap);
      model.addAttribute("error", "Временные технические работы. Roadmap будет улучшен в ближайшее время.");
      return "CareerRoadmap";
    }
  }

  private String escapeHtmlForMarkdown(String text) {
    if (text == null) return "";
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  private Roadmap createSimpleRoadmap(String vacancy, HttpSession session) {
    Roadmap roadmap = new Roadmap();
    List<RoadmapZone> zones = new ArrayList<>();

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

    log.info("✅ Простой roadmap создан");
    return roadmap;
  }

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
}