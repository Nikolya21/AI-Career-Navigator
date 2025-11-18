package com.aicareer.core.DTO.courseDto;

import com.aicareer.core.model.courseModel.CourseRequirements;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CourseRequest {
  private String courseRequirements;
  public CourseRequest(CourseRequirements courseRequirements) {
    this.courseRequirements = courseRequirements.getCourseRequirements();
  }
}