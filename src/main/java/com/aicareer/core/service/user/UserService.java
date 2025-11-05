package com.aicareer.core.service.user;

import com.aicareer.module.user.UserServiceRepository;
import com.aicareer.core.model.user.*;
import com.aicareer.core.DTO.user.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class UserService implements UserServiceRepository {


  // временное хранилище (потом заменим на реализацию UserRepository)
  private final Map<Long, User> userStore = new ConcurrentHashMap<>();
  private final Map<String, User> emailIndex = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);
  private final Logger log = Logger.getLogger(UserService.class.getName());

  private final PasswordEncoder passwordEncoder;

  @Override
  public User registerUser(UserRegistrationDto registrationDto) {
    log.info(String.format("Registering user with email: {%s}", registrationDto.getEmail()));

    if (emailIndex.containsKey(registrationDto.getEmail().toLowerCase())) {
      throw new IllegalArgumentException("Email already registered");
    }

    User user = User.builder()
        .id(idGenerator.getAndIncrement())
        .email(registrationDto.getEmail().toLowerCase())
        .passwordHash(passwordEncoder.encode(registrationDto.getPassword()))
        .name(registrationDto.getName())
        .CV(null)
        .skills(null)
        .vacancyNow(null)
        .build();

    // сохраняем во временном хранилище для тестирования
    userStore.put(user.getId(), user);
    emailIndex.put(user.getEmail(), user);

    log.info(String.format("User registered successfully. ID: {%s}, Email: {%s}", user.getId(), user.getEmail()));

    return user;
  }

  @Override
  public User authenticateUser(LoginRequestDto loginRequest) {
    log.info(String.format("Authentication attempt for email: {%s}", loginRequest.getEmail()));

    User user = emailIndex.get(loginRequest.getEmail().toLowerCase());

    if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
      throw new SecurityException("Invalid email or password");
    }

    log.info(String.format("User authenticated successfully: {%s}", user.getEmail()));
    return user;
  }

  @Override
  public User verifyEmail(String verificationToken) {
    // реализация для проверки адреса электронной почты(отправка письма с проверкой).
    log.info(String.format("Email verification for token: {%s}", verificationToken));
    return null; // потом реализую, сейчас не понимаю как
  }

  @Override
  public UserProfileDto getUserProfile(Long userId) {
    User user = userStore.get(userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found with ID: " + userId);
    }

    UserProfileDto profile = new UserProfileDto();
    profile.setEmail(user.getEmail());
    profile.setName(user.getName());
    profile.setCV(user.getCV());
    profile.setSkills(user.getSkills());

    return profile;
  }

  @Override
  public boolean isEmailAvailable(String email) {
    return !emailIndex.containsKey(email.toLowerCase());
  }

  @Override
  public List<User> getAllUsers() {
    return new ArrayList<>(userStore.values());
  }

  public User findUserById(Long id) {
    return userStore.get(id);
  }

  @Override
  public void uploadCV(File CVFile, Long userId) {
    log.info(String.format("CV upload for user: {%s}", userId));

    User user = userStore.get(userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found with ID: " + userId);
    }

    CVData CV = CVData.builder()
        .id(idGenerator.getAndIncrement())
        .userId(userId)
        .file(CVFile)
        .build();

    user.setCV(CV);
  }

  @Override
  public void updateRealVacancy(String vacancy, Long userId) {
    log.info(String.format("Vacancy update for user: {%s}", userId));

    User user = userStore.get(userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found with ID: " + userId);
    }

    user.setVacancyNow(vacancy);
  }

  @Override
  public void updateSkills(UserSkills skills, Long userId) {
    log.info(String.format("Skills update for user: {%s}", userId));

    User user = userStore.get(userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found with ID: " + userId);
    }

    user.setSkills(skills);
  }
}