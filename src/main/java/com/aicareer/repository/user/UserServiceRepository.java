package com.aicareer.repository.user;

import com.aicareer.core.model.user.User;
import com.aicareer.core.DTO.UserRegistrationDto;
import com.aicareer.core.DTO.LoginRequestDto;
import com.aicareer.core.model.user.UserSkills;
import java.io.File;
import java.util.List;

public interface UserServiceRepository {
  User registerUser(UserRegistrationDto registrationDto);
  User authenticateUser(LoginRequestDto loginRequest);
  User verifyEmail(String verificationToken);
  User getUserProfile(Long userId);
  boolean isEmailAvailable(String email);
  List<User> getAllUsers(); // Для тестирования
  User findUserById(Long id);
  void uploadCV(File cv, Long userId);
  void updateRealVacancy(String vacancy, Long userId);
  void updateSkills(UserSkills skills, Long userId);
}