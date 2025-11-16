package com.aicareer.core.service.user.impl;

import com.aicareer.core.DTO.user.*;
import com.aicareer.core.service.user.UserService;
import com.aicareer.core.service.user.model.AuthenticationResult;
import com.aicareer.core.service.user.model.RegistrationResult;
import com.aicareer.core.service.user.model.UpdateResult;
import com.aicareer.core.service.user.util.*;
import com.aicareer.core.model.user.CVData;
import com.aicareer.core.model.user.User;
import com.aicareer.core.model.user.UserSkills;
import com.aicareer.core.validation.AuthenticationValidator;
import com.aicareer.core.validation.RegistrationValidator;
import com.aicareer.repository.user.CVDataRepository;
import com.aicareer.repository.user.UserRepository;
import com.aicareer.repository.user.UserSkillsRepository;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.util.List;
import java.util.Optional;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final CVDataRepository cvDataRepository;
  private final UserSkillsRepository userSkillsRepository;

  public UserServiceImpl(UserRepository userRepository,
      CVDataRepository cvDataRepository,
      UserSkillsRepository userSkillsRepository) {
    this.userRepository = userRepository;
    this.cvDataRepository = cvDataRepository;
    this.userSkillsRepository = userSkillsRepository;
  }
  @Override
  public RegistrationResult registerUser(UserRegistrationDto registrationDto) {
    List<String> validationErrors = RegistrationValidator.validate(
        registrationDto,
        this::isEmailAvailable
    );

    if (!validationErrors.isEmpty()) {
      return RegistrationResult.error(validationErrors);
    }

    System.out.println("Registering user without errors with email: " + registrationDto.getEmail());

    try {
      User user = User.builder()
          .name(registrationDto.getName())
          .email(registrationDto.getEmail())
          .passwordHash(PasswordEncoder.encode(registrationDto.getPassword()))
          .cv(null)
          .skills(null)
          .vacancyNow(null)
          .userPreferences(null)
          .roadmapId(null)
          .build();

      user.updateTimestamps();
      User savedUser = userRepository.save(user);

      System.out.println("User registered successfully. ID: " + savedUser.getId());
      return RegistrationResult.success(savedUser);

    } catch (Exception e) {
      return RegistrationResult.error(List.of("Системная ошибка: " + e.getMessage()));
    }
  }

  @Override
  public AuthenticationResult authenticateUser(LoginRequestDto loginRequest) {
    List<String> validationErrors = AuthenticationValidator.validate(loginRequest);

    if (!validationErrors.isEmpty()) {
      return AuthenticationResult.error(validationErrors);
    }

    System.out.println("Authentication attempt for email: " + loginRequest.getEmail());

    try {
      Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());

      if (userOpt.isEmpty()) {
        return AuthenticationResult.error(List.of("Неверный email или пароль"));
      }

      User user = userOpt.get();

      if (!PasswordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
        return AuthenticationResult.error(List.of("Неверный email или пароль"));
      }

      System.out.println("User authenticated successfully: " + user.getEmail());
      return AuthenticationResult.success(user);

    } catch (Exception e) {
      return AuthenticationResult.error(List.of("Системная ошибка: " + e.getMessage()));
    }
  }

  @Override
  public User getUserProfile(Long userId) {
    if (userId == null || userId <= 0) {
      throw new IllegalArgumentException("Invalid user ID");
    }

    Optional<User> userOpt = userRepository.findById(userId);
    if (userOpt.isEmpty()) {
      throw new IllegalArgumentException("User not found with ID: " + userId);
    }

    User user = userOpt.get();

    Optional<CVData> cvDataOpt = cvDataRepository.findByUserId(userId);
    Optional<UserSkills> skillsOpt = userSkillsRepository.findByUserId(userId);

    User profile = new User();
    profile.setEmail(user.getEmail());
    profile.setName(user.getName());
    profile.setCv(cvDataOpt.orElse(null));
    profile.setSkills(skillsOpt.orElse(null));
    profile.setUserPreferences(null);
    profile.setVacancyNow(user.getVacancyNow());
    profile.setRoadmapId(user.getRoadmapId());

    return profile;
  }

  @Override
  public UpdateResult updateVacancy(String vacancy, Long userId) {
    if (vacancy == null || vacancy.trim().isEmpty()) {
      return UpdateResult.error("Вакансия не может быть пустой");
    }
    if (vacancy.length() > 255) {
      return UpdateResult.error("Название вакансии слишком длинное");
    }
    if (userId == null || userId <= 0) {
      return UpdateResult.error("Неверный ID пользователя");
    }

    System.out.println("Vacancy update for user: " + userId);

    try {
      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        return UpdateResult.error("Пользователь не найден");
      }

      User user = userOpt.get();
      user.setVacancyNow(vacancy.trim());
      user.updateTimestamps();
      userRepository.save(user);

      System.out.println("Vacancy updated to: " + vacancy);
      return UpdateResult.success();

    } catch (Exception e) {
      return UpdateResult.error("Системная ошибка: " + e.getMessage());
    }
  }

  @Override
  public UpdateResult updateRoadmap(Long roadmapId, Long userId) {
    if (roadmapId == null || roadmapId <= 0) {
      return UpdateResult.error("Неверный ID дорожной карты");
    }
    if (userId == null || userId <= 0) {
      return UpdateResult.error("Неверный ID пользователя");
    }

    System.out.println("Roadmap update for user: " + userId);

    try {
      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        return UpdateResult.error("Пользователь не найден");
      }

      User user = userOpt.get();
      user.setRoadmapId(roadmapId);
      user.updateTimestamps();
      userRepository.save(user);

      System.out.println("Roadmap updated to ID: " + roadmapId);
      return UpdateResult.success();

    } catch (Exception e) {
      return UpdateResult.error("Системная ошибка: " + e.getMessage());
    }
  }

  @Override
  public UpdateResult updateSkills(UserSkills skills, Long userId) {
    if (skills == null) {
      return UpdateResult.error("Навыки не могут быть пустыми");
    }
    if (userId == null || userId <= 0) {
      return UpdateResult.error("Неверный ID пользователя");
    }

    if (skills.getFullCompliancePercentage() < 0 || skills.getFullCompliancePercentage() > 100) {
      return UpdateResult.error("Общий процент соответствия должен быть от 0 до 100");
    }

    System.out.println("Skills update for user: " + userId);

    try {
      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        return UpdateResult.error("Пользователь не найден");
      }

      skills.setUserId(userId);
      skills.updateTimestamps();
      userSkillsRepository.save(skills);

      System.out.println("Skills updated successfully for user ID: " + userId);
      return UpdateResult.success();

    } catch (Exception e) {
      return UpdateResult.error("Системная ошибка: " + e.getMessage());
    }
  }

  @Override
  public UpdateResult uploadCV(File cvFile, Long userId) {
    if (cvFile == null) {
      return UpdateResult.error("Файл не может быть пустым");
    }

    String fileName = cvFile.getName().toLowerCase();
    if (!fileName.endsWith(".pdf") && !fileName.endsWith(".docx")) {
      return UpdateResult.error("Файл должен быть в формате PDF или DOCX");
    }

    if (userId == null || userId <= 0) {
      return UpdateResult.error("Неверный ID пользователя");
    }

    System.out.println("CV upload for user: " + userId);

    try {
      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        return UpdateResult.error("Пользователь не найден");
      }

      String extractedText = UniversalTextExtractor(cvFile);

      CVData cvData = CVData.builder()
          .userId(userId)
          .file(cvFile)
          .information(extractedText)
          .build();

      cvData.updateTimestamps();
      cvDataRepository.save(cvData);

      System.out.println("CV uploaded successfully for user ID: " + userId);
      return UpdateResult.success();

    } catch (Exception e) {
      return UpdateResult.error("Системная ошибка: " + e.getMessage());
    }
  }

  @Override
  public boolean isEmailAvailable(String email) {
    return !userRepository.existsByEmail(email);
  }

  @Override
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  private static String UniversalTextExtractor(File cvFile) throws IOException {
    String fileName = cvFile.getName();

    String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

    switch (fileExtension) {
      case "pdf":
        PDDocument documentPDF = PDDocument.load(cvFile);
        PDFTextStripper pdfStripper = new PDFTextStripper();
        String text = pdfStripper.getText(documentPDF);
        if (documentPDF != null) {
          documentPDF.close();
        }
        return text;
      case "docx":
        try (FileInputStream fis = new FileInputStream(cvFile);
            XWPFDocument document = new XWPFDocument(fis)) { // автоматическое закрытие ресурсов
          List<XWPFParagraph> paragraphs = document.getParagraphs();
          StringBuilder textDOCX = new StringBuilder();
          for (XWPFParagraph para : paragraphs) {
            textDOCX.append(para.getText());
            textDOCX.append("\n"); // добавляем перенос строки для сохранения структуры
          }
          return textDOCX.toString();
        }
      default:
        throw new IllegalArgumentException("Неизвестный или неподдерживаемый формат файла: ." + fileExtension);
    }
  }
}