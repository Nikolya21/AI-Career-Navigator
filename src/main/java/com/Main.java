package com;

import com.aicareer.core.model.user.*;
import com.aicareer.core.DTO.user.*;
import com.aicareer.core.service.user.SkillAnalysisService;
import com.aicareer.core.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Map;

@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})

@Slf4j
public class Main {

  public static void main(String[] args) {
    var context = SpringApplication.run(Main.class, args);

    // Автоматическое тестирование при запуске
    runAutomatedTests(context);
  }

  private static void runAutomatedTests(org.springframework.context.ApplicationContext context) {
    log.info("=== АВТОМАТИЧЕСКОЕ ТЕСТИРОВАНИЕ ===");

    try {
      // Получаем сервисы из контекста Spring
      UserService userService = context.getBean(UserService.class);
      SkillAnalysisService skillService = context.getBean(SkillAnalysisService.class);
      PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);

      testUserRegistration(userService);
      testSkillAnalysis(skillService);
      testPasswordHashing(passwordEncoder);
      testCompleteUserFlow(userService, skillService);

      log.info("🎉 ВСЕ АВТОМАТИЧЕСКИЕ ТЕСТЫ ПРОЙДЕНЫ УСПЕШНО!");

    } catch (Exception e) {
      log.error("❌ Ошибка в автоматическом тестировании: {}", e.getMessage());
      e.printStackTrace();
    }
  }

  private static void testUserRegistration(UserService userService) {
    log.info("\n--- ТЕСТ РЕГИСТРАЦИИ ПОЛЬЗОВАТЕЛЯ ---");

    UserRegistrationDto newUser = new UserRegistrationDto();
    newUser.setEmail("test.user@example.com");
    newUser.setPassword("TestPass123!");
    newUser.setName("Тестовый Пользователь");

    User registeredUser = userService.registerUser(newUser);
    assert registeredUser != null : "Регистрация пользователя не удалась";
    assert registeredUser.getEmail().equals("test.user@example.com") : "Email не совпадает";

    log.info("✅ Регистрация пользователя: УСПЕХ");
  }

  private static void testSkillAnalysis(SkillAnalysisService skillService) {
    log.info("\n--- ТЕСТ АНАЛИЗА НАВЫКОВ ---");

    Map<String, Object> analysis = skillService.analyzeSkillLevel(1L, "Java Developer");
    assert analysis.containsKey("compliancePercentage") : "Анализ не содержит compliancePercentage";
    assert analysis.containsKey("skillGaps") : "Анализ не содержит skillGaps";

    log.info("✅ Анализ навыков: УСПЕХ");
  }

  private static void testPasswordHashing(PasswordEncoder passwordEncoder) {
    log.info("\n--- ТЕСТ ХЕШИРОВАНИЯ ПАРОЛЯ ---");

    String rawPassword = "MySecurePassword123";
    String hashedPassword = passwordEncoder.encode(rawPassword);

    assert !rawPassword.equals(hashedPassword) : "Пароль не был захэширован";
    assert passwordEncoder.matches(rawPassword, hashedPassword) : "Верификация пароля не удалась";

    log.info("✅ Хеширование пароля: УСПЕХ");
  }

  private static void testCompleteUserFlow(UserService userService, SkillAnalysisService skillService) {
    log.info("\n--- ТЕСТ ПОЛНОГО СЦЕНАРИЯ ---");

    try {
      // Регистрация
      UserRegistrationDto userDto = new UserRegistrationDto();
      userDto.setEmail("full.test@example.com");
      userDto.setPassword("FullTest123!");
      userDto.setName("Полный Тест");

      User user = userService.registerUser(userDto);
      log.info("✅ Регистрация: УСПЕХ - ID: {}", user.getId());

      // Аутентификация
      LoginRequestDto loginRequest = new LoginRequestDto();
      loginRequest.setEmail("full.test@example.com");
      loginRequest.setPassword("FullTest123!");

      User authenticated = userService.authenticateUser(loginRequest);
      log.info("✅ Аутентификация: УСПЕХ - {}", authenticated.getName());

      // Анализ навыков
      Map<String, Object> analysis = skillService.analyzeSkillLevel(user.getId(), "Senior Developer");
      log.info("✅ Анализ навыков: УСПЕХ - {}% соответствия", analysis.get("compliancePercentage"));

      log.info("🎉 Полный сценарий пользователя: УСПЕХ");

    } catch (Exception e) {
      log.error("❌ Полный сценарий пользователя: НЕУДАЧА - {}", e.getMessage());
      throw e;
    }
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}