package com.aicareer.core.DTO;

public class CourseRequest {
  private CourseRequirements courseRequirements;

  public CourseRequest(CourseRequirements courseRequirements) {
    this.courseRequirements = courseRequirements;
  }

  public CourseRequirements getCourseRequirements() {
    return courseRequirements;
  }

  public void setCourseRequirements(CourseRequirements courseRequirements) {
    this.courseRequirements = courseRequirements;
  }
}