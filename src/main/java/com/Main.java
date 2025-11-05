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

    // автоматическое тестирование при запуске
    runAutomatedTests(context);
  }

  private static void runAutomatedTests(org.springframework.context.ApplicationContext context) {
    log.info("--- AUTOMATIC TESTING ---");

    try {
      UserService userService = context.getBean(UserService.class);
      SkillAnalysisService skillService = context.getBean(SkillAnalysisService.class);
      PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);

      testUserRegistration(userService);
      testSkillAnalysis(skillService);
      testPasswordHashing(passwordEncoder);
      testCompleteUserFlow(userService, skillService);

      log.info(";) ALL AUTOMATIC TEST PASSED SUCCESSFUL!");

    } catch (Exception e) {
      log.error("-_- ERROR IN AUTOMATIC TESTING: {}", e.getMessage());
      e.printStackTrace();
    }
  }

  private static void testUserRegistration(UserService userService) {
    log.info("\n--- TEST REGISTRATION ---");

    UserRegistrationDto newUser = new UserRegistrationDto();
    newUser.setEmail("test.user@example.com");
    newUser.setPassword("TestPass123!");
    newUser.setName("TEST USER");

    User registeredUser = userService.registerUser(newUser);
    assert registeredUser != null : "Registration of user is failed";
    assert registeredUser.getEmail().equals("test.user@example.com") : "Email not match";

    log.info("<3 Registration of user: SUCCESS");
  }

  private static void testSkillAnalysis(SkillAnalysisService skillService) {
    log.info("\n--- TEST SKILL ANALYSE ---");

    Map<String, Object> analysis = skillService.analyzeSkillLevel(1L, "Java Developer");
    assert analysis.containsKey("compliancePercentage") : "Analyse not contains compliancePercentage";
    assert analysis.containsKey("skillGaps") : "Analyse not contains skillGaps";

    log.info("<3 Analyse skills: SUCCESS");
  }

  private static void testPasswordHashing(PasswordEncoder passwordEncoder) {
    log.info("\n--- TEST OF HASHING OF PASSWORD ---");

    String rawPassword = "MySecurePassword123";
    String hashedPassword = passwordEncoder.encode(rawPassword);

    assert !rawPassword.equals(hashedPassword) : "Password was not hashing";
    assert passwordEncoder.matches(rawPassword, hashedPassword) : "Verification of password failed";

    log.info("<3 Hashing of password: SUCCESS");
  }

  private static void testCompleteUserFlow(UserService userService, SkillAnalysisService skillService) {
    log.info("\n--- FULL SCRIPT TEST ---");

    try {
      // Регистрация
      UserRegistrationDto userDto = new UserRegistrationDto();
      userDto.setEmail("full.test@example.com");
      userDto.setPassword("FullTest123!");
      userDto.setName("Full test");

      User user = userService.registerUser(userDto);
      log.info("<3 Registration: SUCCESS - ID: {}", user.getId());

      // Аутентификация
      LoginRequestDto loginRequest = new LoginRequestDto();
      loginRequest.setEmail("full.test@example.com");
      loginRequest.setPassword("FullTest123!");

      User authenticated = userService.authenticateUser(loginRequest);
      log.info("<3 Authentication: SUCCESS - {}", authenticated.getName());

      // Анализ навыков
      Map<String, Object> analysis = skillService.analyzeSkillLevel(user.getId(), "Senior Developer");
      log.info("<3 Skill Analysis: SUCCESS - {}% Match", analysis.get("compliancePercentage"));

      log.info("))))) Full User Script: SUCCESS");

    } catch (Exception e) {
      log.error("((((( Full User Script: FAILURE - {}", e.getMessage());
      throw e;
    }
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}