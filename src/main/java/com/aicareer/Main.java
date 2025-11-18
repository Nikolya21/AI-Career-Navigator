package com.aicareer;

import com.aicareer.application.CareerNavigatorApplicationImpl;
import com.aicareer.core.config.DatabaseConfig;
import com.aicareer.core.service.course.LearningPlanAssembler;
import com.aicareer.core.service.course.ServiceGenerateCourse;
import com.aicareer.core.service.course.ServicePrompt;
import com.aicareer.core.service.course.ServiceWeek;
import com.aicareer.core.service.course.WeekDistributionService;
import com.aicareer.core.service.parserOfVacancy.*;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.information.ChatWithAiAfterDeterminingVacancyService;
import com.aicareer.core.service.information.ChatWithAiBeforeDeterminingVacancyService;
import com.aicareer.core.service.information.DialogService;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import com.aicareer.core.service.roadmap.RoadmapService;
import com.aicareer.core.service.user.UserService;
import com.aicareer.core.service.user.impl.UserServiceImpl;
import com.aicareer.presentation.ConsolePresentation;
import com.aicareer.repository.user.CVDataRepository;
import com.aicareer.repository.user.UserPreferencesRepository;
import com.aicareer.repository.user.UserRepository;
import com.aicareer.repository.user.UserSkillsRepository;
import com.aicareer.repository.user.impl.CVDataRepositoryImpl;
import com.aicareer.repository.user.impl.UserPreferencesRepositoryImpl;
import com.aicareer.repository.user.impl.UserRepositoryImpl;
import com.aicareer.repository.user.impl.UserSkillsRepositoryImpl;
import com.aicareer.repository.roadmap.RoadmapRepository;
import com.aicareer.repository.roadmap.RoadmapZoneRepository;
import com.aicareer.repository.roadmap.WeekRepository;
import com.aicareer.repository.roadmap.TaskRepository;
import com.aicareer.repository.roadmap.impl.RoadmapRepositoryImpl;
import com.aicareer.repository.roadmap.impl.RoadmapZoneRepositoryImpl;
import com.aicareer.repository.roadmap.impl.WeekRepositoryImpl;
import com.aicareer.repository.roadmap.impl.TaskRepositoryImpl;

import javax.sql.DataSource;

public class Main {
  public static void main(String[] args) {
    try {
      System.out.println("🚀 Запуск AlCareer Application...");

      // 1. Инициализация БД через DatabaseConfig (автоматически создаст таблицы)
      DataSource dataSource = DatabaseConfig.getDataSource();
      System.out.println("✅ База данных инициализирована");

      // 2. Репозитории User
      UserRepository userRepository = new UserRepositoryImpl(dataSource);
      UserPreferencesRepository userPreferencesRepository = new UserPreferencesRepositoryImpl(dataSource);
      CVDataRepository cvDataRepository = new CVDataRepositoryImpl(dataSource);
      UserSkillsRepository userSkillsRepository = new UserSkillsRepositoryImpl(dataSource);

      // 3. Репозитории Roadmap
      RoadmapRepository roadmapRepository = new RoadmapRepositoryImpl(dataSource);
      RoadmapZoneRepository zoneRepository = new RoadmapZoneRepositoryImpl(dataSource);
      WeekRepository weekRepository = new WeekRepositoryImpl(dataSource);
      TaskRepository taskRepository = new TaskRepositoryImpl(dataSource);

      // 4. Сервисы
      UserService userService = new UserServiceImpl(
              userRepository,
              cvDataRepository,
              userSkillsRepository,
              userPreferencesRepository
      );

      RoadmapService roadmapService = new RoadmapService(dataSource);

      GigaChatService gigaChatService = new GigaChatService();
      DialogService dialogService = new DialogService(gigaChatService, true);

      // 5. Сервисы бизнес-логики
      var chatBeforeVacancyService = new ChatWithAiBeforeDeterminingVacancyService(gigaChatService, dialogService);
      var selectVacancy = new SelectVacancy(gigaChatService);
      var chatAfterVacancyService = new ChatWithAiAfterDeterminingVacancyService(gigaChatService, dialogService);
      var roadmapGenerateService = new RoadmapGenerateService(gigaChatService);

      ServicePrompt servicePrompt = new ServicePrompt();
      ServiceGenerateCourse courseGenerator = new ServiceGenerateCourse(servicePrompt, gigaChatService);
      ServiceWeek courseResponseParser = new ServiceWeek();
      WeekDistributionService distributionService = new WeekDistributionService();

      LearningPlanAssembler learningPlanAssembler = new LearningPlanAssembler(
          courseGenerator,
          courseResponseParser,
          distributionService
      );

      // 6. Приложение
      var application = new CareerNavigatorApplicationImpl(
              userService,
              chatBeforeVacancyService,
              selectVacancy,
              chatAfterVacancyService,
              roadmapGenerateService,
              roadmapService,
              userPreferencesRepository,
              cvDataRepository,        // ← ДОБАВЬ
              userSkillsRepository,
              learningPlanAssembler
      );

      // 7. Запуск
      System.out.println("✅ Все компоненты инициализированы");
      System.out.println("🎯 Запуск пользовательского интерфейса...");

      new ConsolePresentation(application).start();

    } catch (Exception e) {
      System.err.println("❌ Критическая ошибка инициализации: " + e.getMessage());
      e.printStackTrace();

      // Полезная информация для диагностики
      System.err.println("\n🔧 Диагностика:");
      System.err.println("- Проверь что PostgreSQL запущен на localhost:5432");
      System.err.println("- Проверь что база 'aicareer' существует");
      System.err.println("- Проверь логин/пароль в application.properties");
      System.err.println("- Проверь сетевые настройки");
    }
  }
}