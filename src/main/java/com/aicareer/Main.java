package com.aicareer;

import com.aicareer.core.DTO.ResponseByWeek;
import com.aicareer.core.model.*;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.information.ChatWithAiAfterDeterminingVacancyService;
import com.aicareer.core.service.information.ChatWithAiBeforeDeterminingVacancyService;
import com.aicareer.core.service.information.DialogService;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import com.aicareer.repository.information.ChatWithAiBeforeDeterminingVacancy;


import java.util.List;


public class Main {

  private static GigaChatService gigaChatService;
  private static DialogService dialogService;
  private static ChatWithAiBeforeDeterminingVacancy chatBeforeVacancyService;
  private static ChatWithAiAfterDeterminingVacancyService chatAfterVacancyService;
  private static RoadmapGenerateService roadmapGenerateService;

  private static FinalVacancyRequirements vacancyRequirements;
  private static CVData cvData;
  private static ResponseByWeek responseByWeek;

  public static void main(String[] args) {
    initializeServices();
//    UserPreferences userPreferences = runBeginAiChatCycle();
//    System.out.println("\n" + "\n" + "\n" + "ВНУТРЕННОСТЬ ОБЪЕКТА UserPreferences");
//    System.out.println(userPreferences.getInfoAboutPerson());
//
//    CourseRequirements courseRequirements = runCourseRequirementsCycle();
//    System.out.println("\n" + "\n" + "\n" + "ВНУТРЕННОСТЬ ОБЪЕКТА CourseRequirements");
//    System.out.println(courseRequirements.getCourseRequirements());

    Roadmap roadmap = runCourseAndRoadmapGenerationCycle();
    System.out.println("\n" + "\n" + "\n" + "ВНУТРЕННОСТЬ ОБЪЕКТА Roadmap");
    System.out.println(roadmap.toString());
  }

  public static ResponseByWeek createTestResponse() {
    ResponseByWeek response = new ResponseByWeek();

    // Неделя 1
    Week week1 = new Week();
    week1.setNumber(1);
    week1.setGoal("Освоить основы Python и основы анализа данных");

    Task task1_1 = new Task();
    task1_1.setDescription("Изучить базовый синтаксис Python");
    task1_1.setUrls(List.of(
            "https://docs.python.org/3/tutorial/",
            "https://www.learnpython.org/"
    ));

    Task task1_2 = new Task();
    task1_2.setDescription("Установить Jupyter Notebook и настроить окружение");
    task1_2.setUrls(List.of("https://jupyter.org/install"));

    week1.setTasks(List.of(task1_1, task1_2));

    // Неделя 2
    Week week2 = new Week();
    week2.setNumber(2);
    week2.setGoal("Изучить библиотеки для анализа данных: Pandas и NumPy");

    Task task2_1 = new Task();
    task2_1.setDescription("Освоить основы работы с Pandas");
    task2_1.setUrls(List.of(
            "https://pandas.pydata.org/docs/",
            "https://www.w3schools.com/python/pandas/default.asp"
    ));

    Task task2_2 = new Task();
    task2_2.setDescription("Практиковаться с NumPy для математических операций");
    task2_2.setUrls(List.of("https://numpy.org/doc/"));

    week2.setTasks(List.of(task2_1, task2_2));

    // Неделя 3
    Week week3 = new Week();
    week3.setNumber(3);
    week3.setGoal("Научиться визуализации данных с Matplotlib и Seaborn");

    Task task3_1 = new Task();
    task3_1.setDescription("Создать первые графики с Matplotlib");
    task3_1.setUrls(List.of("https://matplotlib.org/stable/tutorials/index.html"));

    Task task3_2 = new Task();
    task3_2.setDescription("Изучить Seaborn для статистической визуализации");
    task3_2.setUrls(List.of("https://seaborn.pydata.org/tutorial.html"));

    // Неделя 4
    Week week4 = new Week();
    week4.setNumber(4);
    week4.setGoal("Освоить продвинутые техники анализа данных с Pandas");

    Task task4_1 = new Task();
    task4_1.setDescription("Изучить группировку и агрегацию данных в Pandas");
    task4_1.setUrls(List.of(
            "https://pandas.pydata.org/docs/user_guide/groupby.html",
            "https://www.w3schools.com/python/pandas/pandas_cleaning.asp"
    ));

    Task task4_2 = new Task();
    task4_2.setDescription("Работа с временными рядами и датами");
    task4_2.setUrls(List.of("https://pandas.pydata.org/docs/user_guide/timeseries.html"));

    week4.setTasks(List.of(task4_1, task4_2));

// Неделя 5
    Week week5 = new Week();
    week5.setNumber(5);
    week5.setGoal("Применить статистические методы для анализа данных");

    Task task5_1 = new Task();
    task5_1.setDescription("Изучить статистические функции в SciPy");
    task5_1.setUrls(List.of(
            "https://docs.scipy.org/doc/scipy/tutorial/index.html",
            "https://www.w3schools.com/python/scipy/index.php"
    ));

    Task task5_2 = new Task();
    task5_2.setDescription("Практика статистического анализа реальных данных");
    task5_2.setUrls(List.of("https://realpython.com/python-statistics/"));

    week5.setTasks(List.of(task5_1, task5_2));

// Неделя 6
    Week week6 = new Week();
    week6.setNumber(6);
    week6.setGoal("Освоить машинное обучение на базовом уровне");

    Task task6_1 = new Task();
    task6_1.setDescription("Введение в Scikit-learn и базовые алгоритмы ML");
    task6_1.setUrls(List.of(
            "https://scikit-learn.org/stable/tutorial/index.html",
            "https://www.w3schools.com/python/python_ml_getting_started.asp"
    ));

    Task task6_2 = new Task();
    task6_2.setDescription("Построение первой модели машинного обучения");
    task6_2.setUrls(List.of("https://scikit-learn.org/stable/auto_examples/index.html"));

    week6.setTasks(List.of(task6_1, task6_2));

// Неделя 7
    Week week7 = new Week();
    week7.setNumber(7);
    week7.setGoal("Работа с базами данных и SQL в Python");

    Task task7_1 = new Task();
    task7_1.setDescription("Изучить SQLAlchemy для работы с базами данных");
    task7_1.setUrls(List.of(
            "https://docs.sqlalchemy.org/en/20/tutorial/index.html",
            "https://www.w3schools.com/sql/sql_intro.asp"
    ));

    Task task7_2 = new Task();
    task7_2.setDescription("Практика извлечения и обработки данных из БД");
    task7_2.setUrls(List.of("https://realpython.com/python-sql-libraries/"));

    week7.setTasks(List.of(task7_1, task7_2));

// Неделя 8
    Week week8 = new Week();
    week8.setNumber(8);
    week8.setGoal("Разработка полноценного проекта анализа данных");

    Task task8_1 = new Task();
    task8_1.setDescription("Планирование и проектирование финального проекта");
    task8_1.setUrls(List.of(
            "https://towardsdatascience.com/the-data-science-project-checklist-7d9f911e21e3",
            "https://www.kaggle.com/learn/data-cleaning"
    ));

    Task task8_2 = new Task();
    task8_2.setDescription("Реализация и презентация результатов проекта");
    task8_2.setUrls(List.of("https://www.kaggle.com/learn/data-visualization"));

    week8.setTasks(List.of(task8_1, task8_2));

// Неделя 9
    Week week9 = new Week();
    week9.setNumber(9);
    week9.setGoal("Оптимизация и развертывание data science проектов");

    Task task9_1 = new Task();
    task9_1.setDescription("Оптимизация производительности кода анализа данных");
    task9_1.setUrls(List.of(
            "https://pandas.pydata.org/docs/user_guide/enhancingperf.html",
            "https://realpython.com/fast-flexible-pandas/"
    ));

    Task task9_2 = new Task();
    task9_2.setDescription("Развертывание моделей в production");
    task9_2.setUrls(List.of("https://mlflow.org/docs/latest/tutorials-and-examples/tutorial.html"));

    week9.setTasks(List.of(task9_1, task9_2));

// Неделя 10
    Week week10 = new Week();
    week10.setNumber(10);
    week10.setGoal("Подготовка к карьере в data science и итоговая аттестация");

    Task task10_1 = new Task();
    task10_1.setDescription("Создание портфолио проектов и подготовка резюме");
    task10_1.setUrls(List.of(
            "https://towardsdatascience.com/how-to-build-a-data-science-portfolio-5f566517c79c",
            "https://www.kaggle.com/learn/portfolio-tips"
    ));

    Task task10_2 = new Task();
    task10_2.setDescription("Итоговый проект и оценка полученных навыков");
    task10_2.setUrls(List.of("https://www.projectpro.io/data-science-projects"));

    week10.setTasks(List.of(task10_1, task10_2));

    week3.setTasks(List.of(task3_1, task3_2));

    response.setWeeks(List.of(week1, week2, week3, week4, week5, week6, week7, week8, week9, week10));
    return response;
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

    responseByWeek = createTestResponse();

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


  private static Roadmap runCourseAndRoadmapGenerationCycle() {
    System.out.println("\n🗺️ ЦИКЛ 8: ГЕНЕРАЦИЯ КУРСА И ДОРОЖНОЙ КАРТЫ");

    try {
      System.out.println("🚀 СОЗДАЕМ ПЕРСОНАЛИЗИРОВАННЫЙ КУРС И ROADMAP...");

      String weeksInformation = roadmapGenerateService.gettingWeeksInformation(responseByWeek);

      String resultOfComplexityAndQuantityAnalyze = roadmapGenerateService.informationComplexityAndQuantityAnalyzeAndCreatingZone(weeksInformation);

      List<Week> weeks = responseByWeek.getWeeks();
      List<RoadmapZone> roadmapZones = roadmapGenerateService.splittingWeeksIntoZones(resultOfComplexityAndQuantityAnalyze, weeks);

      Roadmap roadmap = roadmapGenerateService.identifyingThematicallySimilarZones(roadmapZones);

      return roadmap;

    } catch (Exception e) {

      System.out.println("❌ Ошибка в цикле генерации курса и roadmap: {}" + " " + e.getMessage());

      return null;
    }
  }
}