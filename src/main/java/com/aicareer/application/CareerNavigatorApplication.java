// com.aicareer.application.CareerNavigatorApplication.java
package com.aicareer.application;

import com.aicareer.core.dto.courseDto.ResponseByWeek;
import com.aicareer.core.exception.*;
import com.aicareer.core.model.user.User;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.vacancy.FinalVacancyRequirements;
import com.aicareer.core.model.courseModel.CourseRequirements;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.repository.course.AssemblePlan;

public interface CareerNavigatorApplication {

  Long register(String email, String password, String name) throws AuthenticationException;
  Long authenticate(String email, String password) throws AuthenticationException;

  UserPreferences gatherUserPreferences(User user, String cvText) throws ChatException;

  FinalVacancyRequirements selectVacancy(UserPreferences preferences) throws VacancySelectionException;

  User getUserProfile(Long userId);

  CourseRequirements defineCourseRequirements(FinalVacancyRequirements vacancyRequirements) throws CourseDefinitionException;

  Roadmap generateRoadmap(ResponseByWeek responseByWeek, User user) throws RoadmapGenerationException;
}