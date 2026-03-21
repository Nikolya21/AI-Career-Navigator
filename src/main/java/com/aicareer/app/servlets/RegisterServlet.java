package com.aicareer.app.servlets;

import com.aicareer.core.dto.user.LoginRequestDto;
import com.aicareer.core.model.user.User;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.user.UserSkills;
import com.aicareer.core.service.user.model.AuthenticationResult;
import com.aicareer.core.service.user.model.UpdateResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.aicareer.core.dto.user.UserRegistrationDto;
import com.aicareer.core.service.user.UserService;
import com.aicareer.core.service.user.impl.UserServiceImpl;
import com.aicareer.core.service.user.model.RegistrationResult;
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

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

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

      System.out.println("✅ UserService initialized with PostgreSQL repositories");
      return new UserServiceImpl(userRepository, cvDataRepository, userSkillsRepository, userPreferencesRepository);

    } catch (Exception e) {
      System.err.println("❌ Error initializing UserService with real DB: " + e.getMessage());
      e.printStackTrace();
      System.out.println("🔄 Using mock service as fallback");
      return createMockUserService();
    }
  }

  private UserService createMockUserService() {
    return new UserService() {
      @Override
      public RegistrationResult registerUser(UserRegistrationDto registrationDto) {
        System.out.println("Mock: Attempting to register user: " + registrationDto.getEmail());

        // Простая имитация проверки email
        if ("exists@test.com".equals(registrationDto.getEmail())) {
          return RegistrationResult.error(List.of("Пользователь с таким email уже существует"));
        }

        // Имитация успешной регистрации
        System.out.println("Mock: User registered successfully: " + registrationDto.getName());
        return RegistrationResult.success(null);
      }

      // Заглушки для остальных методов
      @Override public AuthenticationResult authenticateUser(LoginRequestDto loginRequest) { return null; }
      @Override public User getUserProfile(Long userId) { return null; }
      @Override public UpdateResult updateVacancy(String vacancy, Long userId) { return null; }
      @Override public UpdateResult updateRoadmap(Long roadmapId, Long userId) { return null; }
      @Override public UpdateResult updateSkills(UserSkills skills, Long userId) { return null; }
      @Override public UpdateResult uploadCV(File cvFile, Long userId) { return null; }
      @Override public boolean isEmailAvailable(String email) { return !"exists@test.com".equals(email); }
      @Override public List<User> getAllUsers() { return List.of(); }
      @Override public UserPreferences getUserPreferences(Long userId) { return null; }
      @Override public UpdateResult updateUserPreferencesInfo(Long userId, String newInfoAboutPerson) { return null; }
      @Override public boolean hasUserPreferences(Long userId) { return false; }
    };
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String name = request.getParameter("name");
    String email = request.getParameter("email");
    String password = request.getParameter("password");
    String confirmPassword = request.getParameter("confirmPassword");

    // Валидация на стороне сервера
    List<String> validationErrors = validateRegistrationData(name, email, password, confirmPassword);

    if (!validationErrors.isEmpty()) {
      request.setAttribute("errors", validationErrors);
      request.setAttribute("email", email);
      request.setAttribute("name", name);
      request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
      return;
    }

    // Создаем DTO для регистрации
    UserRegistrationDto registrationDto = new UserRegistrationDto();
    registrationDto.setEmail(email);
    registrationDto.setPassword(password);
    registrationDto.setName(name);

    System.out.println("🔄 Registering user: " + email);

    // Вызываем сервис для регистрации
    RegistrationResult result = userService.registerUser(registrationDto);

    if (result.isSuccess()) {
      System.out.println("✅ Registration successful for: " + email);
      // Успешная регистрация - перенаправляем на страницу входа с сообщением об успехе
      String encodedEmail = java.net.URLEncoder.encode(email, "UTF-8");
      response.sendRedirect(request.getContextPath() + "/login?registered=true&email=" + encodedEmail);
    } else {
      System.out.println("❌ Registration failed for: " + email + " - " + result.getErrors());
      // Ошибки регистрации - показываем форму снова
      request.setAttribute("errors", result.getErrors());
      request.setAttribute("email", email);
      request.setAttribute("name", name);
      request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
    }
  }

  private List<String> validateRegistrationData(String name, String email, String password, String confirmPassword) {
    List<String> errors = new ArrayList<>();

    if (name == null || name.trim().isEmpty()) {
      errors.add("Имя обязательно для заполнения");
    } else if (name.trim().length() < 2) {
      errors.add("Имя должно содержать минимум 2 символа");
    } else if (!name.matches("[A-Za-zА-Яа-яЁё\\s]+")) {
      errors.add("Имя может содержать только буквы и пробелы");
    }

    if (email == null || email.trim().isEmpty()) {
      errors.add("Email обязателен для заполнения");
    } else if (!isValidEmail(email)) {
      errors.add("Некорректный формат email");
    }

    if (password == null || password.length() < 6) {
      errors.add("Пароль должен содержать минимум 6 символов");
    } else if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d).{6,}$")) {
      errors.add("Пароль должен содержать хотя бы одну букву и одну цифру");
    }

    if (!password.equals(confirmPassword)) {
      errors.add("Пароли не совпадают");
    }

    return errors;
  }

  private boolean isValidEmail(String email) {
    return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
  }
}