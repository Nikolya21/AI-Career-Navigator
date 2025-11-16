package com.aicareer;

import com.aicareer.application.CareerNavigatorApplicationImpl;
import com.aicareer.core.DTO.user.*;
import com.aicareer.core.Validator.LlmResponseValidator;
import com.aicareer.core.config.GigaChatConfig;
import com.aicareer.core.DTO.courseDto.CourseRequest;
import com.aicareer.core.DTO.courseDto.ResponseByWeek;
import com.aicareer.core.model.courseModel.CourseRequirements;
import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.user.CVData;
import com.aicareer.core.model.user.User;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.vacancy.FinalVacancyRequirements;
import com.aicareer.core.model.vacancy.SelectedPotentialVacancy;
import com.aicareer.core.service.ParserOfVacancy.SelectVacancy;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.information.ChatWithAiAfterDeterminingVacancyService;
import com.aicareer.core.service.information.ChatWithAiBeforeDeterminingVacancyService;
import com.aicareer.core.service.information.DialogService;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import com.aicareer.core.service.user.*;
import com.aicareer.core.service.user.util.PasswordEncoder;
import com.aicareer.presentation.ConsolePresentation;
import com.aicareer.repository.user.CVDataRepository;
import com.aicareer.repository.user.UserRepository;
import com.aicareer.core.service.user.UserService;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;
import com.aicareer.core.service.user.impl.UserServiceImpl;
import com.aicareer.repository.user.CVDataRepository;
import com.aicareer.repository.user.UserRepository;
import com.aicareer.repository.user.UserSkillsRepository;
import com.aicareer.repository.user.impl.CVDataRepositoryImpl;
import com.aicareer.repository.user.impl.UserRepositoryImpl;
import com.aicareer.repository.user.impl.UserSkillsRepositoryImpl;
import java.util.List;
import java.util.Scanner;
import javax.sql.DataSource;

// Предполагаемые классы (не импортированы в оригинале, но используются):
// Добавьте их в core, если ещё не сделано:
// import com.aicareer.core.service.user.model.AuthenticationResult;
// import com.aicareer.core.service.user.model.RegistrationResult;

public class Main {
  // === База данных ===
  private static DataSource dataSource;
  private static UserService userService;
  public static void main(String[] args) {
    try {
      // 1. Создаём DataSource
      DataSource dataSource = createPostgresDataSource();

      // 2. Репозитории
      UserSkillsRepository userSkillRepository = new UserSkillsRepositoryImpl(dataSource);
      CVDataRepository cvDataRepository = new CVDataRepositoryImpl(dataSource);
      UserRepository userRepository = new UserRepositoryImpl(dataSource);

      // 4. UserService
      UserService userService = new UserServiceImpl(
        userRepository,
        cvDataRepository,
        userSkillRepository
      );

      // 5. Остальные сервисы
      GigaChatService gigaChatService = new GigaChatService();
      DialogService dialogService = new DialogService(gigaChatService, true);

      var chatBeforeVacancyService = new ChatWithAiBeforeDeterminingVacancyService(gigaChatService, dialogService);
      var selectVacancy = new SelectVacancy();
      var chatAfterVacancyService = new ChatWithAiAfterDeterminingVacancyService(gigaChatService, dialogService);
      var roadmapGenerateService = new RoadmapGenerateService(gigaChatService);

      // 6. Приложение
      var application = new CareerNavigatorApplicationImpl(
        userService,
        chatBeforeVacancyService,
        selectVacancy,
        chatAfterVacancyService,
        roadmapGenerateService
      );

      // 7. Запуск
      new ConsolePresentation(application).start();

    } catch (Exception e) {
      System.err.println("❌ Ошибка инициализации: " + e.getMessage());
      e.printStackTrace();
    }
  }

  // DataSource
  private static DataSource createPostgresDataSource() {
    return new DataSource() {
      @Override
      public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
      }

      @Override
      public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
      }

      private final String url = "jdbc:postgresql://localhost:5432/aicareer";
      private final String username = "postgres";
      private final String password = "password";

      @Override
      public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
      }

      @Override
      public Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
      }

      @Override public PrintWriter getLogWriter() throws SQLException { return null; }
      @Override public void setLogWriter(PrintWriter out) throws SQLException {}
      @Override public void setLoginTimeout(int seconds) throws SQLException {}
      @Override public int getLoginTimeout() throws SQLException { return 0; }
      @Override public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
      }
    };
  }
}