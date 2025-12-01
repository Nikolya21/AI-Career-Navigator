package com.aicareer.presentation;

import com.aicareer.application.CareerNavigatorApplication;
import com.aicareer.application.CareerNavigatorApplicationImpl;
import com.aicareer.core.dto.courseDto.CourseRequest;
import com.aicareer.core.dto.courseDto.ResponseByWeek;
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
      Long currentUserID = handleAuthentication();
      if (currentUserID == null) return;


      // показ всего
      System.out.println("Вы сейчас находитесь в меню презентации.");
      while (true) {
        System.out.println("Выберите страницу:");
        System.out.println("1 - Личный кабинет");
        System.out.println("2 - \"Роудмапа\"");
        System.out.println("3 - Выход");
        System.out.print("Ваш выбор: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
          case "1":
            User currentUser = application.getUserProfile(currentUserID);
            displayUserProfile(currentUser);
            break;
          case "2":
            Roadmap roadmap = application.getSavedRoadmap(currentUserID);
            displaySuccess(roadmap);
            break;
          case "3":
            System.out.println("👋 До свидания!");
            return;
          default:
            System.out.println("❌ Неверный выбор. Попробуйте снова.");
        }
      }
    } catch (Exception e) {
      System.err.println("💥 КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
      e.printStackTrace();
    } finally {
      scanner.close();
    }
  }

  private Long handleAuthentication() {
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

  private Long registerUser() {
    System.out.println("\n📝 Регистрация нового пользователя");
    System.out.print("Введите email: ");
    String email = scanner.nextLine().trim();
    System.out.print("Введите пароль: ");
    String password = scanner.nextLine().trim();
    System.out.print("Введите имя: ");
    String name = scanner.nextLine().trim();
    try {
      return application.register(email, password, name);
    } catch (Exception e) {
      System.err.println("❌ Ошибка регистрации: " + e.getMessage());
      return null;
    }
  }

  private Long loginUser() {
    System.out.println("\n🔑 Аутентификация пользователя");
    System.out.print("Введите email: ");
    String email = scanner.nextLine().trim();
    System.out.print("Введите пароль: ");
    String password = scanner.nextLine().trim();

    try {
      return application.authenticate(email, password); // name не нужен для входа
    } catch (Exception e) {
      System.err.println("❌ Ошибка аутентификации: " + e.getMessage());
      return null;
    }
  }

  private void displaySuccess(Roadmap roadmap) {
    System.out.println("📋 Сгенерированная дорожная карта:");
    System.out.println(roadmap.getRoadmapZones());
  }

  private void displayUserProfile(User user) {
    System.out.println("\nUser ID: " + user.getId());
    System.out.println("Name: " + user.getName());
    System.out.println("Email: " + user.getEmail());
    System.out.println("Selected vacancy: " + user.getVacancyNow() + "\n");
  }
}