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
                {"Демо Пользователь", "demo@aicareer.com", "demo123WW", "Backend Engineer"}
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
                    .weeks(generateWeeksForZone())
                    .build();
            zone.updateTimestamps();
            zones.add(zone);
        }
        return zones;
    }

    private List<Week> generateWeeksForZone() {
        List<Week> weeks = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
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
        String[] taskDescriptions = {"Изучить теоретические материалы", "Выполнить практическое задание"};
        for (int i = 0; i < 2; i++) {
            Task task = Task.builder()
                    .description(taskDescriptions[random.nextInt(taskDescriptions.length)])
                    .urls(Arrays.asList("https://example.com/learning"))
                    .build();
            task.updateTimestamps();
            tasks.add(task);
        }
        return tasks;
    }
}