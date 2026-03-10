package com.aicareer.core.model.courseModel;

import com.aicareer.core.model.user.UserPreferences;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequirements {
  private UserPreferences userPreferences;  // содержит и цель, и профиль обучения
}