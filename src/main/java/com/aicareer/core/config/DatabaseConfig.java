package com.aicareer.core.config;


import java.sql.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class DatabaseConfig {
    private static SimpleDataSource dataSource; // ← Теперь SimpleDataSource вместо DataSource

    public static SimpleDataSource getDataSource() {
        if (dataSource == null) {
            initializeDataSource();
        }
        return dataSource;
    }

    private static void initializeDataSource() {
        // 1. Загружаем настройки из properties файла ✅
      try {
        Class.forName("org.postgresql.Driver");
        System.out.println("DEBUG: Explicitly loaded PostgreSQL driver.");
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("PostgreSQL JDBC Driver not found in Classpath.", e);
      }

        Properties props = loadDatabaseProperties();

        String url = "jdbc:" + props.getProperty("database.url");
        String username = props.getProperty("database.username");
        String password = props.getProperty("database.password");

        // 2. Создаем DataSource
        dataSource = new SimpleDataSource(url, username, password);

        // 3. Тестируем подключение ПЕРЕД инициализацией БД ✅
        if (!testConnection()) {
            throw new RuntimeException("Cannot connect to database. Please check your PostgreSQL configuration.");
        }

        // 4. Инициализируем БД
        initializeDatabase();
    }

    private static Properties loadDatabaseProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new RuntimeException("application.properties not found");
            }
            props.load(input);

            // Проверяем обязательные параметры
            requireProperty(props, "database.url");
            requireProperty(props, "database.username");
            requireProperty(props, "database.password");

            return props;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    private static void requireProperty(Properties props, String key) {
        if (!props.containsKey(key) || props.getProperty(key).trim().isEmpty()) {
            throw new RuntimeException("Required property '" + key + "' is missing in application.properties");
        }
    }

    private static boolean testConnection() {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("✅ Database connection successful");

            // Дополнительная диагностика
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("   URL: " + meta.getURL());
            System.out.println("   User: " + meta.getUserName());
            System.out.println("   Database: " + meta.getDatabaseProductName());
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

    private static void initializeDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute("CREATE SCHEMA IF NOT EXISTS aicareer");
            conn.createStatement().execute("SET search_path TO aicareer");

            // В ПРАВИЛЬНОМ ПОРЯДКЕ (из-за foreign keys)
          executeSqlFile(conn, "db/schema/01_users.sql");
          executeSqlFile(conn, "db/schema/02_cv_data.sql");
          executeSqlFile(conn, "db/schema/03_user_preferences.sql");
          executeSqlFile(conn, "db/schema/04_user_skills.sql");
          executeSqlFile(conn, "db/schema/05_roadmaps.sql");
          executeSqlFile(conn, "db/schema/06_roadmap_zones.sql");
          executeSqlFile(conn, "db/schema/07_weeks.sql");
          executeSqlFile(conn, "db/schema/08_tasks.sql");

            // ✅ ГЕНЕРАЦИЯ ТЕСТОВЫХ ДАННЫХ
            generateTestDataUsingServices();

            System.out.println("✅ Database schema initialized successfully");
        } catch (SQLException e) {
            System.err.println("❌ Failed to initialize database: " + e.getMessage());
        }
    }

    private static void generateTestDataUsingServices() {
        try {
            System.out.println("🎲 Generating test data using services...");

            ServiceDataGenerator generator = new ServiceDataGenerator(dataSource);
            generator.generateAllTestData();

            System.out.println("🎉 Demo accounts created successfully:");
            System.out.println("   👤 demo@aicareer.com / demo123WW");
            System.out.println("   👤 alex@demo.com / hash123WW");
            System.out.println("   👤 maria@demo.com / hash123WW");
            System.out.println("   👤 ivan@demo.com / hash123WW");
            System.out.println("   📊 Each account has: CV data, skills, preferences, and roadmap");

        } catch (Exception e) {
            System.err.println("❌ Error generating test data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void executeSqlFile(Connection conn, String filePath) throws SQLException {
        try (InputStream inputStream = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream(filePath)) {

            if (inputStream == null) {
                System.err.println("⚠️ SQL file not found: " + filePath);
                return;
            }

            String sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            // Улучшенный парсинг SQL ✅
            executeSqlStatements(conn, sql);

            System.out.println("✅ Executed SQL file: " + filePath);

        } catch (IOException e) {
            throw new SQLException("Error reading SQL file: " + filePath, e);
        }
    }

    private static void executeSqlStatements(Connection conn, String sql) throws SQLException {
        // Простой парсер для разделения SQL statements
        String[] statements = sql.split(";(?=(?:[^']*'[^']*')*[^']*$)"); // Игнорирует ; в кавычках

        for (String statement : statements) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(trimmed);
                } catch (SQLException e) {
                    // Логируем но продолжаем выполнение (возможно объект уже существует)
                    System.err.println("⚠️ SQL execution warning: " + e.getMessage());
                }
            }
        }
    }

    // Новый метод для мониторинга
    public static void printConnectionStats() {
        if (dataSource != null) {
            System.out.println("Active database connections: " + dataSource.getActiveConnections());
        }
    }

    // Метод для закрытия всех соединений (при shutdown)
    public static void close() {
        if (dataSource != null) {
            // В улучшенной версии SimpleDataSource можно добавить shutdown логику
            System.out.println("Database connections closed");
        }
    }
}