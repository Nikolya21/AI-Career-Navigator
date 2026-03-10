package com.aicareer.presentation;

import com.aicareer.application.CareerNavigatorApplicationImpl;
import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.core.model.user.User;
import com.aicareer.core.exception.RoadmapGenerationException;

import java.util.List;
import java.util.Scanner;

public class ConsolePresentation {

  private final CareerNavigatorApplicationImpl application;
  private final Scanner scanner;

  // ANSI-коды для цветов (если терминал поддерживает)
  private static final String ANSI_RESET = "\u001B[0m";
  private static final String ANSI_RED = "\u001B[31m";
  private static final String ANSI_GREEN = "\u001B[32m";
  private static final String ANSI_YELLOW = "\u001B[33m";
  private static final String ANSI_BLUE = "\u001B[34m";
  private static final String ANSI_PURPLE = "\u001B[35m";
  private static final String ANSI_CYAN = "\u001B[36m";
  private static final String ANSI_BOLD = "\u001B[1m";

  public ConsolePresentation(CareerNavigatorApplicationImpl application) {
    this.application = application;
    this.scanner = new Scanner(System.in);
  }

  public void start() {
    printHeader("🚀 AI-Career Navigator: Полный end-to-end цикл");

    try {
      Long currentUserID = handleAuthentication();
      if (currentUserID == null) {
        printInfo("До свидания!");
        return;
      }

      printInfo("Вы в главном меню.");
      while (true) {
        System.out.println();
        printBold("Выберите страницу:");
        System.out.println("1. 📋 Личный кабинет");
        System.out.println("2. 🗺️ Дорожная карта (Roadmap)");
        System.out.println("3. 🚪 Выход");
        System.out.print("👉 Ваш выбор: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
          case "1":
            User currentUser = application.getUserProfile(currentUserID);
            displayUserProfile(currentUser);
            break;
          case "2":
            displayRoadmap(currentUserID);
            break;
          case "3":
            printSuccess("До свидания!");
            return;
          default:
            printError("Неверный выбор. Попробуйте снова.");
        }
      }
    } catch (Exception e) {
      printError("Критическая ошибка: " + e.getMessage());
      e.printStackTrace();
    } finally {
      scanner.close();
    }
  }

  private Long handleAuthentication() {
    printHeader("🔐 Регистрация / Аутентификация");
    while (true) {
      System.out.println();
      printBold("Выберите действие:");
      System.out.println("1. 📝 Регистрация");
      System.out.println("2. 🔑 Вход");
      System.out.println("3. 🚪 Выход");
      System.out.print("👉 Ваш выбор: ");
      String choice = scanner.nextLine().trim();

      switch (choice) {
        case "1":
          Long userId = registerUser();
          if (userId != null) return userId;
          break;
        case "2":
          userId = loginUser();
          if (userId != null) return userId;
          break;
        case "3":
          return null;
        default:
          printError("Неверный выбор. Попробуйте снова.");
      }
      // При ошибке регистрации/входа предлагаем повторить или вернуться
      System.out.println();
      printInfo("Хотите попробовать снова? (1 - да, 2 - вернуться в меню выхода)");
      System.out.print("👉 Ваш выбор: ");
      String retry = scanner.nextLine().trim();
      if ("2".equals(retry)) {
        return null;
      }
    }
  }

  private Long registerUser() {
    printBold("📝 Регистрация нового пользователя");
    System.out.print("Введите email: ");
    String email = scanner.nextLine().trim();
    System.out.print("Введите пароль: ");
    String password = scanner.nextLine().trim();
    System.out.print("Введите имя: ");
    String name = scanner.nextLine().trim();

    try {
      Long userId = application.register(email, password, name);
      printSuccess("Регистрация успешна! Ваш ID: " + userId);
      return userId;
    } catch (Exception e) {
      printError("Ошибка регистрации: " + e.getMessage());
      return null;
    }
  }

  private Long loginUser() {
    printBold("🔑 Аутентификация");
    System.out.print("Введите email: ");
    String email = scanner.nextLine().trim();
    System.out.print("Введите пароль: ");
    String password = scanner.nextLine().trim();

    try {
      Long userId = application.authenticate(email, password);
      printSuccess("Вход выполнен успешно! Ваш ID: " + userId);
      return userId;
    } catch (Exception e) {
      printError("Ошибка аутентификации: " + e.getMessage());
      return null;
    }
  }

  private void displayUserProfile(User user) {
    printHeader("📋 Личный кабинет");
    System.out.println("🆔 ID: " + user.getId());
    System.out.println("👤 Имя: " + (user.getName() != null ? user.getName() : "не указано"));
    System.out.println("📧 Email: " + user.getEmail());
    System.out.println("💼 Выбранная вакансия: " + (user.getVacancyNow() != null ? user.getVacancyNow() : "не выбрана"));
    System.out.println("🗺️ ID дорожной карты: " + (user.getRoadmapId() != null ? user.getRoadmapId() : "не сгенерирована"));
  }

  private void displayRoadmap(Long userId) {
    printHeader("🗺️ Дорожная карта");
    try {
      Roadmap roadmap = application.getSavedRoadmap(userId);
      if (roadmap == null) {
        printError("Дорожная карта не найдена.");
        return;
      }
      printRoadmap(roadmap);
    } catch (RoadmapGenerationException e) {
      printError("Ошибка при загрузке дорожной карты: " + e.getMessage());
    } catch (Exception e) {
      printError("Неизвестная ошибка: " + e.getMessage());
    }
  }

  private void printRoadmap(Roadmap roadmap) {
    System.out.println("🔹 Roadmap ID: " + roadmap.getId());
    System.out.println("👤 Пользователь ID: " + roadmap.getUserId());
    System.out.println("📅 Создана: " + roadmap.getCreatedAt());
    System.out.println("🔄 Обновлена: " + roadmap.getUpdatedAt());

    List<RoadmapZone> zones = roadmap.getRoadmapZones();
    if (zones == null || zones.isEmpty()) {
      printInfo("В дорожной карте нет зон.");
      return;
    }

    System.out.println();
    printBold("Зоны обучения:");
    for (int i = 0; i < zones.size(); i++) {
      RoadmapZone zone = zones.get(i);
      printZone(zone, i + 1);
      if (i < zones.size() - 1) {
        System.out.println("  " + "─".repeat(50));
      }
    }
  }

  private void printZone(RoadmapZone zone, int number) {
    System.out.println();
    printBold("  Зона " + number + ": " + zone.getName());
    System.out.println("  🎯 Цель: " + (zone.getLearningGoal() != null ? zone.getLearningGoal() : "не указана"));
    System.out.println("  📊 Уровень сложности: " + (zone.getComplexityLevel() != null ? zone.getComplexityLevel() : "не указан"));
    System.out.println("  🗓️ Порядок: " + zone.getZoneOrder());

    List<Week> weeks = zone.getWeeks();
    if (weeks == null || weeks.isEmpty()) {
      System.out.println("     Нет недель в этой зоне.");
      return;
    }

    System.out.println("     Недели:");
    for (int i = 0; i < weeks.size(); i++) {
      Week week = weeks.get(i);
      printWeek(week, i + 1);
    }
  }

  private void printWeek(Week week, int weekNumber) {
    System.out.println("       📅 Неделя " + weekNumber + ": " + (week.getGoal() != null ? week.getGoal() : "цель не указана"));
    List<Task> tasks = week.getTasks();
    if (tasks == null || tasks.isEmpty()) {
      System.out.println("          Нет задач на эту неделю.");
      return;
    }
    System.out.println("          Задачи:");
    for (int i = 0; i < tasks.size(); i++) {
      Task task = tasks.get(i);
      printTask(task, i + 1);
    }
  }

  private void printTask(Task task, int taskNumber) {
    System.out.println("            🔹 Задача " + taskNumber + ": " + task.getDescription());
    List<String> urls = task.getUrls();
    if (urls != null && !urls.isEmpty()) {
      System.out.println("               Ресурсы:");
      for (int j = 0; j < urls.size(); j++) {
        System.out.println("                 - " + urls.get(j));
      }
    }
  }

  // Вспомогательные методы для цветного вывода
  private void printHeader(String text) {
    System.out.println();
    System.out.println(ANSI_CYAN + ANSI_BOLD + "══════════════════════════════════════════════════" + ANSI_RESET);
    System.out.println(ANSI_CYAN + ANSI_BOLD + "  " + text + ANSI_RESET);
    System.out.println(ANSI_CYAN + ANSI_BOLD + "══════════════════════════════════════════════════" + ANSI_RESET);
  }

  private void printBold(String text) {
    System.out.println(ANSI_BOLD + text + ANSI_RESET);
  }

  private void printSuccess(String text) {
    System.out.println(ANSI_GREEN + "✅ " + text + ANSI_RESET);
  }

  private void printError(String text) {
    System.out.println(ANSI_RED + "❌ " + text + ANSI_RESET);
  }

  private void printInfo(String text) {
    System.out.println(ANSI_YELLOW + "ℹ️ " + text + ANSI_RESET);
  }
}