package com.aicareer.core.service.user;

import com.aicareer.core.dto.user.*;
import com.aicareer.core.model.user.User;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.user.UserSkills;
import com.aicareer.core.service.user.model.AuthenticationResult;
import com.aicareer.core.service.user.model.RegistrationResult;
import com.aicareer.core.service.user.model.UpdateResult;
import java.io.File;
import java.util.List;

public interface UserService {
  // === АУТЕНТИФИКАЦИЯ И РЕГИСТРАЦИЯ ===
  RegistrationResult registerUser(UserRegistrationDto registrationDto);
  AuthenticationResult authenticateUser(LoginRequestDto loginRequest);
  boolean isEmailAvailable(String email);

  User getUserProfile(Long userId);
  List<User> getAllUsers();

  UserPreferences getUserPreferences(Long userId);

  UpdateResult updateUserPreferencesInfo(Long userId, String newInfoAboutPerson);

  boolean hasUserPreferences(Long userId);

  UpdateResult updateVacancy(String vacancy, Long userId);
  UpdateResult updateRoadmap(Long roadmapId, Long userId);

  UpdateResult uploadCV(File cv, Long userId);
  UpdateResult updateSkills(UserSkills skills, Long userId);
}