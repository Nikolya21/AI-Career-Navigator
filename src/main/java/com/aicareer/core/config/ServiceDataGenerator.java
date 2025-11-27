package com.aicareer.core.config;

import com.aicareer.core.model.user.*;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.service.roadmap.RoadmapService;
import com.aicareer.core.service.user.util.PasswordEncoder;
import com.aicareer.repository.user.*;
import com.aicareer.repository.user.impl.*;
import javax.sql.DataSource;
import java.util.*;

public class ServiceDataGenerator {
    private final DataSource dataSource;
    private final UserRepository userRepository;
    private final Random random = new Random();

    public ServiceDataGenerator(DataSource dataSource) {
        this.dataSource = dataSource;
        this.userRepository = new UserRepositoryImpl(dataSource);
    }

    public void generateAllTestData() {
        System.out.println("🎲 Generating test data using services...");

        // Очищаем существующие тестовые данные
        cleanupTestData();

        // Создаем пользователей
        List<User> testUsers = generateUsers();

        // Для каждого пользователя создаем полный профиль (БЕЗ SKILLS)
        for (User user : testUsers) {
            generateUserProfile(user);
        }

        System.out.println("✅ Test data generated successfully");
    }

    private void cleanupTestData() {
        System.out.println("🧹 Cleaning up TEST data only...");

        try {
            List<User> allUsers = userRepository.findAll();
            int deletedCount = 0;

            for (User user : allUsers) {
                if (isTestUser(user)) {
                    // Удаляем пользователя - каскадно удалятся все связанные данные
                    userRepository.delete(user.getId());
                    deletedCount++;
                    System.out.println("🗑️ Deleted test user: " + user.getEmail());
                }
            }

            System.out.println("✅ Deleted " + deletedCount + " test users");

        } catch (Exception e) {
            System.err.println("❌ Error during test data cleanup: " + e.getMessage());
        }
    }

    private boolean isTestUser(User user) {
        return user.getEmail().endsWith("@demo.com") ||
                user.getEmail().equals("demo@aicareer.com");
    }

    private List<User> generateUsers() {
        List<User> users = new ArrayList<>();
        String[][] userData = {
                {"Алексей Демо", "alex@demo.com", "hash123WW", "Java Developer"},
                {"Мария Тестова", "maria@demo.com", "hash123WW", "Frontend Developer"},
                {"Иван Примеров", "ivan@demo.com", "hash123WW", "Fullstack Developer"},
                {"Демо Пользователь", "demo@aicareer.com", "demo123WW", "Backend Engineer"},
                {"Екатерина Смирнова", "ekaterina@demo.com", "hash123WW", "Data Scientist"},
                {"Дмитрий Петров", "dmitry@demo.com", "hash123WW", "DevOps Engineer"},
                {"Ольга Козлова", "olga@demo.com", "hash123WW", "Mobile Developer"},
                {"Сергей Иванов", "sergey@demo.com", "hash123WW", "QA Engineer"},
                {"Анна Сидорова", "anna@demo.com", "hash123WW", "Team Lead"},
                {"Павел Николаев", "pavel@demo.com", "hash123WW", "Software Architect"}
        };

        for (String[] data : userData) {
            try {
                User user = User.builder()
                        .name(data[0])
                        .email(data[1])
                        .passwordHash(PasswordEncoder.encode(data[2]))
                        .vacancyNow(data[3])
                        .build();
                user.updateTimestamps();

                User savedUser = userRepository.save(user);
                users.add(savedUser);
                System.out.println("👤 Created user: " + savedUser.getEmail());

            } catch (Exception e) {
                System.err.println("❌ Failed to create user " + data[1] + ": " + e.getMessage());
            }
        }

        return users;
    }

    private void generateUserProfile(User user) {
        // ✅ ТОЛЬКО CV данные, preferences и roadmap (БЕЗ SKILLS)
        generateCVData(user);
        generateUserPreferences(user);
        generateRoadmapForUser(user);

        // ❌ УБРАЛИ generateUserSkills(user)
    }

    private void generateCVData(User user) {
        try {
            CVDataRepository cvRepo = new CVDataRepositoryImpl(dataSource);
            CVData cvData = CVData.builder()
                    .userId(user.getId())
                    .information(getCVInformation(user.getVacancyNow()))
                    .build();
            cvData.updateTimestamps();
            cvRepo.save(cvData);
            System.out.println("📝 Created CV data for user: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Error creating CV data for " + user.getEmail() + ": " + e.getMessage());
        }
    }

    private void generateUserPreferences(User user) {
        try {
            UserPreferencesRepository prefsRepo = new UserPreferencesRepositoryImpl(dataSource);
            UserPreferences preferences = UserPreferences.builder()
                    .userId(user.getId())
                    .infoAboutPerson(getUserPreferencesInfo(user.getVacancyNow()))
                    .build();
            prefsRepo.save(preferences);
            System.out.println("⚙️ Created preferences for user: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Error creating preferences for " + user.getEmail() + ": " + e.getMessage());
        }
    }

    // ❌ УБРАЛИ ВЕСЬ МЕТОД generateUserSkills

    private void generateRoadmapForUser(User user) {
        try {
            RoadmapService roadmapService = new RoadmapService(dataSource);
            Roadmap roadmap = Roadmap.builder()
                    .userId(user.getId())
                    .build();
            roadmap.updateTimestamps();

            List<RoadmapZone> zones = generateRoadmapZones();
            roadmap.setRoadmapZones(zones);

            Roadmap savedRoadmap = roadmapService.saveCompleteRoadmap(roadmap);
            user.setRoadmapId(savedRoadmap.getId());
            userRepository.save(user);

            System.out.println("🗺️ Created roadmap for user: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Error creating roadmap for " + user.getEmail() + ": " + e.getMessage());
        }
    }

    // Вспомогательные методы без изменений...
    private String getCVInformation(String vacancy) {
        Map<String, String> cvTemplates = new HashMap<>();
        cvTemplates.put("Java Developer",
                "Опытный Java-разработчик с 5+ лет опыта. Специализация: Spring Boot, микросервисы, PostgreSQL.");
        cvTemplates.put("Frontend Developer",
                "Frontend разработчик с глубокими знаниями React и TypeScript. Опыт работы в Agile-командах.");
        cvTemplates.put("Fullstack Developer",
                "Fullstack developer с опытом работы как на бэкенде (Java), так и на фронтенде (React).");
        cvTemplates.put("Backend Engineer",
                "Backend engineer с фокусом на создании масштабируемых API.");
        cvTemplates.put("Data Scientist",
                "Data Scientist с опытом в машинном обучении и анализе больших данных. Владение Python, Pandas, Scikit-learn.");
        cvTemplates.put("DevOps Engineer",
                "DevOps инженер с опытом настройки CI/CD, контейнеризации и облачных технологий.");
        cvTemplates.put("Mobile Developer",
                "Mobile разработчик с опытом создания приложений для iOS и Android на React Native.");
        cvTemplates.put("QA Engineer",
                "QA инженер с глубокими знаниями автоматизированного тестирования и процессов обеспечения качества.");
        cvTemplates.put("Team Lead",
                "Team Lead с опытом управления командами разработки и организации Agile-процессов.");
        cvTemplates.put("Software Architect",
                "Software Architect с экспертизой в проектировании масштабируемых систем и выборе технологических решений.");

        return cvTemplates.getOrDefault(vacancy, "Информация о профессиональном опыте и навыках.");
    }

    private String getUserPreferencesInfo(String vacancy) {
        Map<String, String> preferenceTemplates = new HashMap<>();
        preferenceTemplates.put("Java Developer",
                "Предпочитаю практический подход к обучению. Интересуюсь микросервисной архитектурой.");
        preferenceTemplates.put("Frontend Developer",
                "Нравится работать над UI/UX, уделяю внимание деталям. Хочу развиваться в направлении Team Lead.");
        preferenceTemplates.put("Fullstack Developer",
                "Ищу баланс между глубокими техническими знаниями и управленческими навыками.");
        preferenceTemplates.put("Backend Engineer",
                "Ценю чистый код и лучшие практики разработки.");
        preferenceTemplates.put("Data Scientist",
                "Увлекаюсь анализом данных и машинным обучением. Хочу углубиться в нейросети.");
        preferenceTemplates.put("DevOps Engineer",
                "Интересуюсь автоматизацией процессов и облачными технологиями. Предпочитаю hands-on подход.");
        preferenceTemplates.put("Mobile Developer",
                "Люблю создавать удобные мобильные интерфейсы. Интересуюсь кросс-платформенной разработкой.");
        preferenceTemplates.put("QA Engineer",
                "Внимателен к деталям, ценю качество кода. Хочу развивать навыки автоматизации тестирования.");
        preferenceTemplates.put("Team Lead",
                "Стремлюсь к развитию лидерских качеств и управленческих навыков вместе с технической экспертизой.");
        preferenceTemplates.put("Software Architect",
                "Интересуюсь системным проектированием и выбором оптимальных архитектурных решений.");

        return preferenceTemplates.getOrDefault(vacancy, "Информация о предпочтениях в обучении.");
    }

    private List<RoadmapZone> generateRoadmapZones() {
        List<RoadmapZone> zones = new ArrayList<>();
        String[][] zoneData = {
                {"Основы программирования", "Изучение базовых концепций", "BEGINNER"},
                {"Фреймворки и инструменты", "Освоение популярных фреймворков", "INTERMEDIATE"},
                {"Продвинутые темы", "Углубленное изучение архитектуры", "ADVANCED"}
        };

        for (int i = 0; i < zoneData.length; i++) {
            RoadmapZone zone = RoadmapZone.builder()
                    .name(zoneData[i][0])
                    .learningGoal(zoneData[i][1])
                    .complexityLevel(zoneData[i][2])
                    .zoneOrder(i + 1)
                    .weeks(generateWeeksForZone(zoneData[i][0],  i + 1))
                    .build();
            zone.updateTimestamps();
            zones.add(zone);
        }
        return zones;
    }

    private List<Week> generateWeeksForZone(String zoneName, int zoneOrder) {
        List<Week> weeks = new ArrayList<>();
        Map<String, List<String[]>> zoneWeekData = new HashMap<>();

        // Данные для разных зон roadmap
        zoneWeekData.put("Основы программирования", Arrays.asList(
                new String[]{
                        "Основы синтаксиса и структуры программы",
                        "Изучение базового синтаксиса, переменных, типов данных и структуры программы"
                },
                new String[]{
                        "Управляющие конструкции и функции",
                        "Освоение условий, циклов, функций и основ отладки"
                }
        ));

        zoneWeekData.put("Фреймворки и инструменты", Arrays.asList(
                new String[]{
                        "Знакомство с фреймворком и его экосистемой",
                        "Изучение архитектуры фреймворка, установка и настройка окружения"
                },
                new String[]{
                        "Практическое применение фреймворка",
                        "Создание первого приложения, работа с основными компонентами"
                }
        ));

        zoneWeekData.put("Продвинутые темы", Arrays.asList(
                new String[]{
                        "Архитектурные паттерны и лучшие практики",
                        "Изучение продвинутых архитектурных решений и оптимизации"
                },
                new String[]{
                        "Подготовка к реальным проектам",
                        "Решение сложных задач, код-ревью и рефакторинг"
                }
        ));

        // Получаем данные для текущей зоны или используем значения по умолчанию
        List<String[]> weekData = zoneWeekData.getOrDefault(zoneName, Arrays.asList(
                new String[]{"Изучение основных концепций", "Освоение фундаментальных принципов"},
                new String[]{"Практическое применение", "Закрепление знаний на практике"}
        ));

        for (int i = 0; i < weekData.size(); i++) {
            Week week = Week.builder()
                    .number(i + 1)
                    .goal(weekData.get(i)[0] + " - " + weekData.get(i)[1])
                    .tasks(generateTasksForWeek(zoneName, i + 1, zoneOrder))
                    .build();
            week.updateTimestamps();
            weeks.add(week);
        }
        return weeks;
    }

    private List<Task> generateTasksForWeek(String zoneName, int weekNumber, int zoneOrder) {
        List<Task> tasks = new ArrayList<>();
        Map<String, List<String[]>> taskTemplates = new HashMap<>();

        // Реальные учебные ресурсы
        Map<String, List<String>> learningResources = new HashMap<>();
        learningResources.put("Основы программирования", Arrays.asList(
                "https://learnjava.one/lessons/basic-syntax",
                "https://javarush.com/lectures/java-basics",
                "https://metanit.com/java/tutorial/",
                "https://vertex-academy.com/tutorials/ru/osnovy-java-s-nulya/"
        ));

        learningResources.put("Фреймворки и инструменты", Arrays.asList(
                "https://spring.io/guides/gs/spring-boot",
                "https://spring.io/guides",
                "https://habr.com/ru/hub/spring_framework/",
                "https://www.baeldung.com/spring-tutorial"
        ));

        learningResources.put("Продвинутые темы", Arrays.asList(
                "https://refactoring.guru/ru/design-patterns",
                "https://martinfowler.com/articles/",
                "https://habr.com/ru/flows/architecture/",
                "https://microservices.io/patterns/"
        ));

        // Описания задач для разных зон и недель
        taskTemplates.put("Основы программирования_1", Arrays.asList(
                new String[]{
                        "Изучение синтаксиса и типов данных",
                        "Пройдите уроки по базовому синтаксису, объявлению переменных и работе с примитивными типами данных"
                },
                new String[]{
                        "Практика с операторами",
                        "Решите 10-15 задач на арифметические и логические операторы"
                },
                new String[]{
                        "Написание первой программы",
                        "Создайте программу 'Hello World' и изучите структуру Java-приложения"
                }
        ));

        taskTemplates.put("Основы программирования_2", Arrays.asList(
                new String[]{
                        "Условия и ветвления",
                        "Изучите конструкции if-else, switch-case и решите практические задачи"
                },
                new String[]{
                        "Циклы и итерации",
                        "Освойте циклы for, while, do-while. Решите задачи на обработку последовательностей"
                },
                new String[]{
                        "Функции и методы",
                        "Научитесь объявлять и использовать методы, изучите передачу параметров"
                }
        ));

        taskTemplates.put("Фреймворки и инструменты_1", Arrays.asList(
                new String[]{
                        "Установка и настройка окружения",
                        "Установите фреймворк, настройте IDE и создайте базовый проект"
                },
                new String[]{
                        "Изучение базовой архитектуры",
                        "Пройдите туториал по основным компонентам и их взаимодействию"
                },
                new String[]{
                        "Создание Hello World приложения",
                        "Реализуйте простое приложение для понимания workflow фреймворка"
                }
        ));

        taskTemplates.put("Фреймворки и инструменты_2", Arrays.asList(
                new String[]{
                        "Работа с основными модулями",
                        "Изучите ключевые модули фреймворка на практических примерах"
                },
                new String[]{
                        "Интеграция с базами данных",
                        "Настройте подключение к БД и реализуйте CRUD операции"
                },
                new String[]{
                        "Создание REST API",
                        "Разработайте простое REST API с использованием изученных технологий"
                }
        ));

        taskTemplates.put("Продвинутые темы_1", Arrays.asList(
                new String[]{
                        "Изучение архитектурных паттернов",
                        "Разберите MVC, MVVM, Clean Architecture на практических кейсах"
                },
                new String[]{
                        "Принципы SOLID и DRY",
                        "Проанализируйте код на соответствие принципам и выполните рефакторинг"
                },
                new String[]{
                        "Оптимизация производительности",
                        "Изучите методы профилирования и оптимизации приложений"
                }
        ));

        taskTemplates.put("Продвинутые темы_2", Arrays.asList(
                new String[]{
                        "Решение комплексной задачи",
                        "Разработайте полноценное приложение с использованием изученных паттернов"
                },
                new String[]{
                        "Код-ревью и рефакторинг",
                        "Проведите анализ чужого кода и предложите улучшения"
                },
                new String[]{
                        "Подготовка проекта к продакшену",
                        "Настройте деплоймент, мониторинг и логирование"
                }
        ));

        String key = zoneName + "_" + weekNumber;
        List<String[]> taskDescriptions = taskTemplates.getOrDefault(key, Arrays.asList(
                new String[]{"Изучение теоретических материалов", "Освойте базовые концепции темы"},
                new String[]{"Практическое задание", "Примените знания на практике"}
        ));

        List<String> resources = learningResources.getOrDefault(zoneName,
                Arrays.asList("https://example.com/learning"));

        for (String[] taskDesc : taskDescriptions) {
            Task task = Task.builder()
                    .description(taskDesc[0] + ". " + taskDesc[1])
                    .urls(Collections.singletonList(resources.get(random.nextInt(resources.size()))))
                    .build();
            task.updateTimestamps();
            tasks.add(task);
        }

        return tasks;
    }
}