package com;

import com.aicareer.core.DTO.ResponseByWeek;
import com.aicareer.core.model.*;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.information.ChatWithAiAfterDeterminingVacancyService;
import com.aicareer.core.service.information.ChatWithAiBeforeDeterminingVacancyService;
import com.aicareer.core.service.information.DialogService;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import com.aicareer.repository.information.ChatWithAiBeforeDeterminingVacancy;


import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;


public class Main {

  private static GigaChatService gigaChatService;
  private static DialogService dialogService;
  private static ChatWithAiBeforeDeterminingVacancy chatBeforeVacancyService;
  private static ChatWithAiAfterDeterminingVacancyService chatAfterVacancyService;
  private static RoadmapGenerateService roadmapGenerateService;

  private static FinalVacancyRequirements vacancyRequirements;
  private static CVData cvData;

  public static void main(String[] args) {
    initializeServices();
    runBeginAiChatCycle();
  }

  private static void initializeServices() {
    System.out.println("1. ИНИЦИАЛИЗАЦИЯ СЕРВИСОВ...");

    // Инициализация DataSource
    //DataSource dataSource = setupDataSource();

    // Инициализация репозиториев
    //RoadmapRepository roadmapRepository = new RoadmapRepositoryImpl(dataSource);


    // Инициализация сервисов
    gigaChatService = new GigaChatService();
    cvData = new CVData();
    cvData.setInformation(
            "Петров Алексей Сергеевич\n" +
            "\n" +
            "Цель: Замещение должности Java-разработчика\n" +
            "\n" +
            "Контактная информация:\n" +
            "Телефон: +7 (999) 765-43-21\n" +
            "Email: petrov.as@example.com\n" +
            "Город: Санкт-Петербург\n" +
            "\n" +
            "Образование:\n" +
            "Высшее, Санкт-Петербургский национальный исследовательский университет информационных технологий, механики и оптики\n" +
            "Факультет: Информационных технологий и программирования\n" +
            "Специальность: Программная инженерия\n" +
            "Год окончания: 2020\n" +
            "\n" +
            "Опыт работы:\n" +
            "Период: июль 2020 — настоящее время\n" +
            "Должность: Java-разработчик\n" +
            "Компания: ООО ТехноСофт\n" +
            "\n" +
            "Обязанности:\n" +
            "Разработка и поддержка backend-части веб-приложений\n" +
            "Участие в проектировании архитектуры системы\n" +
            "Написание unit-тестов\n" +
            "Код-ревью\n" +
            "Оптимизация производительности приложений\n" +
            "\n" +
            "Профессиональные навыки:\n" +
            "Языки программирования: Java Kotlin SQL\n" +
            "Фреймворки: Spring Boot Hibernate JUnit\n" +
            "Базы данных: PostgreSQL MySQL Redis\n" +
            "Инструменты: Git Maven Docker Jenkins\n" +
            "Методологии: Agile Scrum\n" +
            "Английский язык: Upper-Intermediate\n" +
            "\n" +
            "Дополнительная информация:\n" +
            "Участие в opensource-проектах\n" +
            "Наличие профиля на GitHub\n" +
            "Готов к релокации");

    vacancyRequirements = new FinalVacancyRequirements("Java, Spring Framework, SQL, Hibernate, Maven, Git, REST API, MySQL/PostgreSQL, Linux, Английский A2+, Опыт 1-3 года, Командная работа, Микросервисы, Docker, JUnit, Интеграционное тестирование, ООП, Паттерны проектирования, Системы контроля версий, Промышленная разработка");

    dialogService = new DialogService(gigaChatService, true);

    chatBeforeVacancyService = new ChatWithAiBeforeDeterminingVacancyService(gigaChatService, dialogService);
    chatAfterVacancyService = new ChatWithAiAfterDeterminingVacancyService(gigaChatService, dialogService);

    roadmapGenerateService = new RoadmapGenerateService(gigaChatService);

    System.out.println("✅ Все сервисы инициализированы");
  }

  private static UserPreferences runBeginAiChatCycle() { //вместо CVdata должен быть какойто сервис,связанный с User
    System.out.println("\n💬 ЦИКЛ 2: ЗНАКОМСТВО С ПОЛЬЗОВАТЕЛЕМ ЧЕРЕЗ AI-ЧАТ");

    try {
      // Симуляция чата с AI
      chatBeforeVacancyService.starDialogWithUser();

      chatBeforeVacancyService.askingStandardQuestions();


      List<String> personalizedQuestions = chatBeforeVacancyService.generatePersonalizedQuestions(cvData);
      chatBeforeVacancyService.askingPersonalizedQuestions(personalizedQuestions);

      UserPreferences userPreferences = chatBeforeVacancyService.analyzeCombinedData();

      return userPreferences;

    } catch (Exception e) {
      System.out.println("❌ Ошибка в цикле AI-чата: {}" + " " + e.getMessage());
      return null;
    }
  }

  private static UserPreferences runSummarizingAiChatCycle() {
    System.out.println("\n💬 ЦИКЛ 4: ФОРМИРОВАНИЕ ВСПОМОГАТЕЛЬНОЙ ИНФОРМАЦИИ НА ОСНОВЕ ДИАЛОГА С ПОЛЬЗОВАТЕЛЕМ ЧЕРЕЗ AI-ЧАТ");

    try {
      // Симуляция чата с AI
      chatBeforeVacancyService.starDialogWithUser();

      chatBeforeVacancyService.askingStandardQuestions();


      List<String> personalizedQuestions = chatBeforeVacancyService.generatePersonalizedQuestions(cvData);
      chatBeforeVacancyService.askingPersonalizedQuestions(personalizedQuestions);

      UserPreferences userPreferences = chatBeforeVacancyService.analyzeCombinedData();

      return userPreferences;

    } catch (Exception e) {

      System.out.println("❌ Ошибка в цикле AI-чата: {}" + " " + e.getMessage());

      return null;
    }
  }

  private static CourseRequirements runCourseRequirementsCycle() {
    System.out.println("\n🎓 ЦИКЛ 6: ФОРМИРОВАНИЕ ТРЕБОВАНИЙ К КУРСУ ОБУЧЕНИЯ ЧЕРЕЗ РЕЗЮМИРУЮЩИЙ ДИАЛОГ");

    try {
      System.out.println("📝 ФОРМИРУЕМ ТРЕБОВАНИЯ К КУРСУ НА ОСНОВЕ ФИНАЛЬНОГО ДИАЛОГА И ИНФОРМАЦИИ, СОБРАННОЙ НА ПРЕДЫДУЩИХ ШАГАХ...");

      List<String> personalizedQuestions = chatAfterVacancyService.generatePersonalizedQuestions(vacancyRequirements);

      chatAfterVacancyService.askingPersonalizedQuestions(personalizedQuestions);

      // AI формирует требования к учебному курсу
      CourseRequirements courseRequirements = chatAfterVacancyService.analyzeCombinedData(vacancyRequirements);


      return courseRequirements;

    } catch (Exception e) {
      System.out.println("❌ Ошибка в цикле формирования требований к курсу: {}" + " " + e.getMessage());
      return null;
    }
  }


//  private static Roadmap runCourseAndRoadmapGenerationCycle() {
//    System.out.println("\n🗺️ ЦИКЛ 8: ГЕНЕРАЦИЯ КУРСА И ДОРОЖНОЙ КАРТЫ");
//
//    try {
//      System.out.println("🚀 СОЗДАЕМ ПЕРСОНАЛИЗИРОВАННЫЙ КУРС И ROADMAP...");
//
//      String weeksInformation = roadmapGenerateService.gettingWeeksInformation(responseByWeek);
//
//      String resultOfComplexityAndQuantityAnalyze = roadmapGenerateService.informationComplexityAndQuantityAnalyzeAndCreatingZone(weeksInformation);
//
//      List<Week> weeks = responseByWeek.getWeeks();
//      List<RoadmapZone> roadmapZones = roadmapGenerateService.splittingWeeksIntoZones(resultOfComplexityAndQuantityAnalyze, weeks);
//
//      Roadmap roadmap = roadmapGenerateService.identifyingThematicallySimilarZones(roadmapZones);
//
//      return roadmap;
//
//    } catch (Exception e) {
//
//      System.out.println("❌ Ошибка в цикле генерации курса и roadmap: {}" + " " + e.getMessage());
//
//      return null;
//    }
//  }
}