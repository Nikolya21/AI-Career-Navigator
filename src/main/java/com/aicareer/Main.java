package com.aicareer;

import com.aicareer.application.CareerNavigatorApplicationImpl;
import com.aicareer.core.service.ParserOfVacancy.SelectVacancy;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.information.ChatWithAiAfterDeterminingVacancyService;
import com.aicareer.core.service.information.ChatWithAiBeforeDeterminingVacancyService;
import com.aicareer.core.service.information.DialogService;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import com.aicareer.core.service.user.*;
import com.aicareer.core.service.user.PasswordEncoder;
import com.aicareer.presentation.ConsolePresentation;
import com.aicareer.repository.user.CVDataRepository;
import com.aicareer.repository.user.UserRepository;
import com.aicareer.repository.user.UserSkillRepository;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class Main {

  public static void main(String[] args) {
    try {
      // 1. Создаём DataSource
      DataSource dataSource = createPostgresDataSource();

      // 2. Репозитории
      UserSkillRepository userSkillRepository = new UserSkillRepositoryImpl(dataSource);
      CVDataRepository cvDataRepository = new CVDataRepositoryImpl(dataSource);
      UserRepository userRepository = new UserRepositoryImpl(dataSource, userSkillRepository);

      // 3. PasswordEncoder
      PasswordEncoder passwordEncoder = new PasswordEncoder() {
        @Override
        public String encode(String rawPassword) {
          return "hashed_" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String encodedPassword) {
          return ("hashed_" + rawPassword).equals(encodedPassword);
        }
      };

      // 4. UserService
      UserService userService = new UserService(
        userRepository,
        cvDataRepository,
        userSkillRepository,
        passwordEncoder
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