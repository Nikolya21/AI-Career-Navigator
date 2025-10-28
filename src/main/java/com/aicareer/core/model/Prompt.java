package com.aicareer.core.model;

import com.aicareer.core.DTO.CourseRequest;

public class Prompt {
  public CourseRequest courseRequest;

  public Prompt(CourseRequest courseRequest) {
    this.courseRequest = courseRequest;
  }

  public CourseRequest getCourseRequest() {
    return courseRequest;
  }

  public void setCourseRequest(CourseRequest courseRequest) {
    this.courseRequest = courseRequest;
  }

}
//  public String generatePrompt() {
//    return "I want you to pretend to be an IT expert. "
//      + "I'll provide you with all the necessary information about the job opening and information about student" + courseRequirements +", and your goal is to create a detailed weekly "
//      + "training plan for the individual. "
//      + "You should draw on your knowledge of career development and training. Using clear, simple, and understandable language in your answers will be helpful for people of all skill "
//      + "levels. It's helpful to present tasks step by step and using bullet points. Try to avoid too much technical detail, but use it when necessary. "
//      + "I want you to format your response as JSON with the keys: \"week_1\", \"week_2\", ..., \"week_10\".";
//  }
