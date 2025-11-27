package com.aicareer.app.servlets;

import com.aicareer.core.dto.user.UserRegistrationDto;
import com.aicareer.core.model.user.User;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.user.UserSkills;
import com.aicareer.core.service.user.model.RegistrationResult;
import com.aicareer.core.service.user.model.UpdateResult;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.aicareer.core.dto.user.LoginRequestDto;
import com.aicareer.core.service.user.UserService;
import com.aicareer.core.service.user.impl.UserServiceImpl;
import com.aicareer.core.service.user.model.AuthenticationResult;
import com.aicareer.core.validator.user.AuthenticationValidator;
import com.aicareer.repository.user.impl.UserRepositoryImpl;
import com.aicareer.repository.user.impl.CVDataRepositoryImpl;
import com.aicareer.repository.user.impl.UserSkillsRepositoryImpl;
import com.aicareer.repository.user.impl.UserPreferencesRepositoryImpl;
import com.aicareer.core.config.DatabaseConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  private UserService userService;

  @Override
  public void init() throws ServletException {
    super.init();
    this.userService = createUserService();
  }

  private UserService createUserService() {
    try {
      // Используем ваш существующий DatabaseConfig
      var dataSource = DatabaseConfig.getDataSource();

      // Создаем репозитории с реальной БД
      var userRepository = new UserRepositoryImpl(dataSource);
      var cvDataRepository = new CVDataRepositoryImpl(dataSource);
      var userSkillsRepository = new UserSkillsRepositoryImpl(dataSource);
      var userPreferencesRepository = new UserPreferencesRepositoryImpl(dataSource);

      System.out.println("✅ LoginServlet: UserService initialized with PostgreSQL repositories");
      return new UserServiceImpl(userRepository, cvDataRepository, userSkillsRepository, userPreferencesRepository);

    } catch (Exception e) {
      System.err.println("❌ LoginServlet: Error initializing UserService: " + e.getMessage());
      e.printStackTrace();
      return createMockUserService();
    }
  }

  private UserService createMockUserService() {
    return new UserService() {
      @Override
      public AuthenticationResult authenticateUser(LoginRequestDto loginRequest) {
        System.out.println("Mock: Authentication attempt for: " + loginRequest.getEmail());

        // Mock данные для тестирования
        if ("user@test.com".equals(loginRequest.getEmail()) && "password123".equals(loginRequest.getPassword())) {
          System.out.println("Mock: Authentication successful");
          return AuthenticationResult.success(null);
        } else {
          System.out.println("Mock: Authentication failed");
          return AuthenticationResult.error(List.of("Неверный email или пароль"));
        }
      }

      // Заглушки для остальных методов
      @Override public RegistrationResult registerUser(UserRegistrationDto registrationDto) { return null; }
      @Override public User getUserProfile(Long userId) { return null; }
      @Override public UpdateResult updateVacancy(String vacancy, Long userId) { return null; }
      @Override public UpdateResult updateRoadmap(Long roadmapId, Long userId) { return null; }
      @Override public UpdateResult updateSkills(UserSkills skills, Long userId) { return null; }
      @Override public UpdateResult uploadCV(File cvFile, Long userId) { return null; }
      @Override public boolean isEmailAvailable(String email) { return true; }
      @Override public List<User> getAllUsers() { return List.of(); }
      @Override public UserPreferences getUserPreferences(Long userId) { return null; }
      @Override public UpdateResult updateUserPreferencesInfo(Long userId, String newInfoAboutPerson) { return null; }
      @Override public boolean hasUserPreferences(Long userId) { return false; }
    };
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.getRequestDispatcher("/jsp/login.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String email = request.getParameter("email");
    String password = request.getParameter("password");

    System.out.println("🔄 Login attempt for email: " + email);

    // Создаем DTO для аутентификации
    LoginRequestDto loginRequest = new LoginRequestDto(email, password);

    // Вызываем сервис для аутентификации
    AuthenticationResult result = userService.authenticateUser(loginRequest);

    if (result.isSuccess()) {
      // Успешная аутентификация
      System.out.println("✅ Login successful for: " + email);

      // Сохраняем информацию о пользователе в сессии
      HttpSession session = request.getSession();
      session.setAttribute("user", result.getUser());
      session.setAttribute("userEmail", email);
      session.setAttribute("authenticated", true);

      // Перенаправляем на страницу диалога
      response.sendRedirect(request.getContextPath() + "/send-message");
    } else {
      // Ошибки аутентификации - показываем форму снова
      System.out.println("❌ Login failed for: " + email + " - " + result.getErrors());
      request.setAttribute("errors", result.getErrors());
      request.setAttribute("email", email);
      request.getRequestDispatcher("/jsp/login.jsp").forward(request, response);
    }
    HttpSession session = request.getSession();
    session.setAttribute("user", result.getUser());
    session.setAttribute("userEmail", email);
    session.setAttribute("authenticated", true);

    String userName = email.split("@")[0];
    session.setAttribute("userName", userName);

    session.setAttribute("registrationDate", new Date());
  }
}