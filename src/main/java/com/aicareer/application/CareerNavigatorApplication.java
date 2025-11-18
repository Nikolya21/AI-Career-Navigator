// com.aicareer.application.CareerNavigatorApplication.java
package com.aicareer.application;

import com.aicareer.core.DTO.courseDto.ResponseByWeek;
import com.aicareer.core.exception.*;
import com.aicareer.core.model.user.User;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.vacancy.FinalVacancyRequirements;
import com.aicareer.core.model.courseModel.CourseRequirements;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.repository.course.AssemblePlan;

public interface CareerNavigatorApplication {

  User authenticateOrRegister(String email, String password, String name) throws AuthenticationException;

  UserPreferences gatherUserPreferences(User user, String cvText) throws ChatException;

  FinalVacancyRequirements selectVacancy(UserPreferences preferences) throws VacancySelectionException;

  CourseRequirements defineCourseRequirements(FinalVacancyRequirements vacancyRequirements) throws CourseDefinitionException;

  Roadmap generateRoadmap(ResponseByWeek responseByWeek) throws RoadmapGenerationException;
}