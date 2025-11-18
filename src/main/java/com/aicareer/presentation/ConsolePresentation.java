package com.aicareer.presentation;

import com.aicareer.application.CareerNavigatorApplication;
import com.aicareer.application.CareerNavigatorApplicationImpl;
import com.aicareer.core.DTO.courseDto.CourseRequest;
import com.aicareer.core.DTO.courseDto.ResponseByWeek;
import com.aicareer.core.model.user.User;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.vacancy.FinalVacancyRequirements;
import com.aicareer.core.model.courseModel.CourseRequirements;
import com.aicareer.core.model.roadmap.Roadmap;

import java.util.Scanner;

public class ConsolePresentation {

  private final CareerNavigatorApplicationImpl application;
  private final Scanner scanner;

  public ConsolePresentation(CareerNavigatorApplicationImpl application) {
    this.application = application;
    this.scanner = new Scanner(System.in);
  }

  public void start() {
    System.out.println("🚀 AI-Career Navigator: Полный end-to-end цикл");
    System.out.println("================================================");

    try {
      User currentUser = handleAuthentication();
      if (currentUser == null) return;

      UserPreferences userPreferences = handleUserPreferences(currentUser);
      if (userPreferences == null) return;

      FinalVacancyRequirements vacancyRequirements = handleVacancySelection(userPreferences);
      if (vacancyRequirements == null) return;

      CourseRequirements courseRequirements = handleCourseDefinition(vacancyRequirements);
      if (courseRequirements == null) return;

      System.out.println("\n📚 Передаём требования в генератор курса...");
      CourseRequest courseRequest = new CourseRequest(courseRequirements);
      ResponseByWeek responseByWeek = application.getLearningPlanAssembler().assemblePlan(courseRequest);
      System.out.println("✅ Курс сгенерирован: " + responseByWeek.getWeeks().size() + " недель");

      Roadmap roadmap = handleRoadmapGeneration(responseByWeek);
      if (roadmap == null) return;

      displaySuccess(roadmap);

    } catch (Exception e) {
      System.err.println("💥 КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
      e.printStackTrace();
    } finally {
      scanner.close();
    }
  }

  private User handleAuthentication() {
    System.out.println("\n🔐 Цикл: Регистрация/Аутентификация");
    while (true) {
      System.out.println("\nВыберите действие:");
      System.out.println("1 - Регистрация");
      System.out.println("2 - Вход");
      System.out.println("3 - Выход");
      System.out.print("Ваш выбор: ");
      String choice = scanner.nextLine().trim();

      switch (choice) {
        case "1":
          return registerUser();
        case "2":
          return loginUser();
        case "3":
          System.out.println("👋 До свидания!");
          return null;
        default:
          System.out.println("❌ Неверный выбор. Попробуйте снова.");
      }
    }
  }

  private User registerUser() {
    System.out.println("\n📝 Регистрация нового пользователя");
    System.out.print("Введите email: ");
    String email = scanner.nextLine().trim();
    System.out.print("Введите пароль: ");
    String password = scanner.nextLine().trim();
    System.out.print("Введите имя: ");
    String name = scanner.nextLine().trim();

    try {
      return application.authenticateOrRegister(email, password, name);
    } catch (Exception e) {
      System.err.println("❌ Ошибка регистрации: " + e.getMessage());
      return null;
    }
  }

  private User loginUser() {
    System.out.println("\n🔑 Аутентификация пользователя");
    System.out.print("Введите email: ");
    String email = scanner.nextLine().trim();
    System.out.print("Введите пароль: ");
    String password = scanner.nextLine().trim();

    try {
      return application.authenticateOrRegister(email, password, ""); // name не нужен для входа
    } catch (Exception e) {
      System.err.println("❌ Ошибка аутентификации: " + e.getMessage());
      return null;
    }
  }

  private UserPreferences handleUserPreferences(User user) {
    System.out.println("\n💬 Цикл: Знакомство с пользователем (AI-чат)");
    String cvText = "Петров Алексей Сергеевич\nЦель: Замещение должности Java-разработчика..."; // Можно сделать ввод с консоли
    try {
      return application.gatherUserPreferences(user, cvText);
    } catch (Exception e) {
      System.err.println("❌ Ошибка в AI-знакомстве: " + e.getMessage());
      return null;
    }
  }

  private FinalVacancyRequirements handleVacancySelection(UserPreferences preferences) {
    System.out.println("\n🎯 Цикл: Подбор и анализ вакансии");
    try {
      return application.selectVacancy(preferences);
    } catch (Exception e) {
      System.err.println("❌ Ошибка при подборе вакансии: " + e.getMessage());
      return null;
    }
  }

  private CourseRequirements handleCourseDefinition(FinalVacancyRequirements vacancyRequirements) {
    System.out.println("\n🎓 Цикл: Формирование требований к курсу");
    try {
      return application.defineCourseRequirements(vacancyRequirements);
    } catch (Exception e) {
      System.err.println("❌ Ошибка при формировании CourseRequirements: " + e.getMessage());
      return null;
    }
  }

  private Roadmap handleRoadmapGeneration(ResponseByWeek responseByWeek) {
    System.out.println("\n🗺️ Цикл: Генерация учебного плана и дорожной карты");
    try {
      return application.generateRoadmap(responseByWeek);
    } catch (Exception e) {
      System.err.println("❌ Ошибка при генерации Roadmap: " + e.getMessage());
      return null;
    }
  }

  private void displaySuccess(Roadmap roadmap) {
    System.out.println("\n✅ УСПЕХ: полный цикл завершён!");
    System.out.println("📋 Сгенерированная дорожная карта:");
    System.out.println(roadmap.getRoadmapZones());
  }
}