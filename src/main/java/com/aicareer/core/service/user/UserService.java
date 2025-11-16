package com.aicareer.core.service.user;

import com.aicareer.core.DTO.user.*;
import com.aicareer.core.model.user.User;
import com.aicareer.core.model.user.UserSkills;
import com.aicareer.core.service.user.model.AuthenticationResult;
import com.aicareer.core.service.user.model.RegistrationResult;
import com.aicareer.core.service.user.model.UpdateResult;
import java.io.File;
import java.util.List;

public interface UserService {
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