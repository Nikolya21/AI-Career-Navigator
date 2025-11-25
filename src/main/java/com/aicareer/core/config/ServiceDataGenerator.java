package com.aicareer.core.config;

import com.aicareer.core.model.user.*;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.service.roadmap.RoadmapService;
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

        // Создаем пользователей
        List<User> testUsers = generateUsers();

        // Для каждого пользователя создаем полный профиль
        for (User user : testUsers) {
            generateUserProfile(user);
        }

        System.out.println("✅ Test data generated successfully using services");
    }

    private List<User> generateUsers() {
        List<User> users = new ArrayList<>();
        String[][] userData = {
                {"Алексей Демо", "alex@demo.com", "hash123", "Java Developer"},
                {"Мария Тестова", "maria@demo.com", "hash123", "Frontend Developer"},
                {"Иван Примеров", "ivan@demo.com", "hash123", "Fullstack Developer"},
                {"Демо Пользователь", "demo@aicareer.com", "demo123", "Backend Engineer"}
        };

        for (String[] data : userData) {
            User user = User.builder()
                    .name(data[0])
                    .email(data[1])
                    .passwordHash(data[2])
                    .vacancyNow(data[3])
                    .build();
            user.updateTimestamps();

            User savedUser = userRepository.save(user);
            users.add(savedUser);
            System.out.println("👤 Created user: " + savedUser.getEmail());
        }

        return users;
    }

    private void generateUserProfile(User user) {
        // 1. Создаем CV данные
        generateCVData(user);

        // 2. Создаем предпочтения
        generateUserPreferences(user);

        // 3. Создаем навыки
        generateUserSkills(user);

        // 4. Создаем роадмап
        generateRoadmapForUser(user);
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

    private void generateUserSkills(User user) {
        try {
            UserSkillsRepository skillsRepo = new UserSkillsRepositoryImpl(dataSource);

            UserSkills skills = UserSkills.builder()
                    .userId(user.getId())
                    .fullCompliancePercentage(40.0 + random.nextDouble() * 50.0) // 40-90%
                    .skillGaps(generateSkillGaps(user.getVacancyNow()))
                    .build();
            skills.updateTimestamps();

            skillsRepo.save(skills);

            System.out.println("💡 Created skills for user: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Error creating skills for " + user.getEmail() + ": " + e.getMessage());
        }
    }

    private void generateRoadmapForUser(User user) {
        try {
            RoadmapService roadmapService = new RoadmapService(dataSource);

            // Создаем основную roadmap
            Roadmap roadmap = Roadmap.builder()
                    .userId(user.getId())
                    .build();
            roadmap.updateTimestamps();

            // Создаем зоны
            List<RoadmapZone> zones = generateRoadmapZones();
            roadmap.setRoadmapZones(zones);

            // Сохраняем полную иерархию через сервис
            Roadmap savedRoadmap = roadmapService.saveCompleteRoadmap(roadmap);

            // Обновляем пользователя с roadmap_id
            user.setRoadmapId(savedRoadmap.getId());
            userRepository.save(user);

            System.out.println("🗺️ Created roadmap for user: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Error creating roadmap for " + user.getEmail() + ": " + e.getMessage());
        }
    }

    // Вспомогательные методы для генерации контента (без изменений)
    private String getCVInformation(String vacancy) {
        Map<String, String> cvTemplates = new HashMap<>();
        cvTemplates.put("Java Developer",
                "Опытный Java-разработчик с 5+ лет опыта. Специализация: Spring Boot, микросервисы, PostgreSQL. " +
                        "Участвовал в разработке высоконагруженных систем. Знание Hibernate, Maven, Git.");
        cvTemplates.put("Frontend Developer",
                "Frontend разработчик с глубокими знаниями React и TypeScript. Опыт работы в Agile-командах. " +
                        "Знание Redux, Webpack, Sass. Участвовал в создании SPA приложений.");
        cvTemplates.put("Fullstack Developer",
                "Fullstack developer с опытом работы как на бэкенде (Java), так и на фронтенде (React). " +
                        "Знаю Docker, Kubernetes, AWS. Участвовал в полном цикле разработки проектов.");
        cvTemplates.put("Backend Engineer",
                "Backend engineer с фокусом на создании масштабируемых API. Опыт работы с базами данных, " +
                        "кэшированием, оптимизацией производительности. Знание SQL, NoSQL, message queues.");

        return cvTemplates.getOrDefault(vacancy, "Информация о профессиональном опыте и навыках.");
    }

    private String getUserPreferencesInfo(String vacancy) {
        Map<String, String> preferenceTemplates = new HashMap<>();
        preferenceTemplates.put("Java Developer",
                "Предпочитаю практический подход к обучению. Интересуюсь микросервисной архитектурой и cloud технологиями. " +
                        "Хочу углубить знания в Spring Ecosystem и DevOps практиках.");
        preferenceTemplates.put("Frontend Developer",
                "Нравится работать над UI/UX, уделяю внимание деталям. Хочу развиваться в направлении Team Lead. " +
                        "Интересуюсь современными фреймворками и инструментами разработки.");
        preferenceTemplates.put("Fullstack Developer",
                "Ищу баланс между глубокими техническими знаниями и управленческими навыками. " +
                        "Интересуюсь полным циклом разработки и архитектурой приложений.");
        preferenceTemplates.put("Backend Engineer",
                "Ценю чистый код и лучшие практики разработки. Хочу углубить знания в области DevOps, " +
                        "безопасности и масштабирования систем.");

        return preferenceTemplates.getOrDefault(vacancy, "Информация о предпочтениях в обучении и карьерном развитии.");
    }

    private Map<String, Double> generateSkillGaps(String vacancy) {
        Map<String, Map<String, Double>> skillGapTemplates = new HashMap<>();

        skillGapTemplates.put("Java Developer", Map.of(
                "Spring Boot", 0.75,
                "Microservices", 0.60,
                "Docker", 0.80,
                "Kubernetes", 0.90
        ));

        skillGapTemplates.put("Frontend Developer", Map.of(
                "React Hooks", 0.65,
                "TypeScript", 0.55,
                "State Management", 0.70,
                "Testing", 0.80
        ));

        skillGapTemplates.put("Fullstack Developer", Map.of(
                "System Design", 0.70,
                "API Design", 0.60,
                "Database Optimization", 0.75,
                "Cloud Services", 0.85
        ));

        skillGapTemplates.put("Backend Engineer", Map.of(
                "Performance Optimization", 0.65,
                "Security", 0.80,
                "Message Queues", 0.70,
                "CI/CD", 0.75
        ));

        return skillGapTemplates.getOrDefault(vacancy, Map.of("General Skills", 0.50));
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
                    .weeks(generateWeeksForZone())
                    .build();
            zone.updateTimestamps();
            zones.add(zone);
        }

        return zones;
    }

    private List<Week> generateWeeksForZone() {
        List<Week> weeks = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            Week week = Week.builder()
                    .number(i)
                    .goal("Цель на неделю " + i + ": освоение ключевых концепций")
                    .tasks(generateTasksForWeek())
                    .build();
            week.updateTimestamps();
            weeks.add(week);
        }

        return weeks;
    }

    private List<Task> generateTasksForWeek() {
        List<Task> tasks = new ArrayList<>();
        String[] taskDescriptions = {
                "Изучить теоретические материалы",
                "Выполнить практическое задание",
                "Пройти онлайн-курс",
                "Подготовить мини-проект"
        };

        for (int i = 0; i < 2; i++) {
            Task task = Task.builder()
                    .description(taskDescriptions[random.nextInt(taskDescriptions.length)])
                    .urls(Arrays.asList("https://example.com/learning", "https://example.com/practice"))
                    .build();
            task.updateTimestamps();
            tasks.add(task);
        }

        return tasks;
    }

}