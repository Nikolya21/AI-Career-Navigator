package com.aicareer.repository.user;

import com.aicareer.core.model.user.User;
import com.aicareer.core.DTO.UserRegistrationDto;
import com.aicareer.core.DTO.LoginRequestDto;
import com.aicareer.core.model.user.UserSkills;
import com.aicareer.core.service.user.AuthenticationResult;
import com.aicareer.core.service.user.RegistrationResult;
import com.aicareer.core.service.user.UpdateResult;
import java.io.File;
import java.util.List;

public interface UserServiceRepository {
  RegistrationResult registerUser(UserRegistrationDto registrationDto);
  AuthenticationResult authenticateUser(LoginRequestDto loginRequest);
  User getUserProfile(Long userId);
  UpdateResult updateVacancy(String vacancy, Long userId);
  UpdateResult updateRoadmap(Long roadmapId, Long userId);
  boolean isEmailAvailable(String email);
  List<User> getAllUsers();
  UpdateResult uploadCV(File cv, Long userId);
  UpdateResult updateSkills(UserSkills skills, Long userId);
}