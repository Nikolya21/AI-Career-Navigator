package com.aicareer.core.service.course;

import com.aicareer.core.DTO.CourseRequest;
import com.aicareer.core.Validator.SyntaxValidator;
import com.aicareer.repository.course.PromptGenerator;

public class ServicePrompt implements PromptGenerator {

  @Override
  public String generatePrompt(CourseRequest request) {
    if (!SyntaxValidator.validate(request)) {
      throw new IllegalArgumentException("Validation failed. Cannot generate prompt.");
    }
    return "You are an expert curriculum designer with 15+ years of experience in software education.\n" +
      "\n" +
      "You will receive a structured input called CourseRequirements, which defines exact specifications for a personalized programming course, including:\n" +
      "\n" +
      "Core topics and modules,\n" +
      "Required practical assignments and projects,\n" +
      "Expected learning outcomes,\n" +
      "Recommended duration,\n" +
      "Success criteria,\n" +
      "AND user context: current skill level, motivation, weekly available time, fears, and knowledge gaps.\n" +
      "Your task: Design a highly practical weekly learning plan that fully satisfies these requirements.\n" +
      "\n" +
      "Rules:\n" +
      "\n" +
      "Break the course into weekly blocks (week1, week2, ..., up to the recommended duration).\n" +
      "Each week must be a single line in the following exact format:\n" +
      "weekN: goal: \"[clear weekly goal]\". task1: \"[specific hands-on task]\". urls: \"[comma-separated trusted URLs]\". task2: \"[another task]\". urls: \"[relevant URLs]\" ..." +
      "Include 1 to 3 practical tasks per week, depending on the user’s available time.\n" +
      "Each task must be concrete, actionable, and project-oriented (e.g., “Build a to-do list”, not “Read about arrays”).\n" +
      "Provide dedicated, high-quality, free resources for each task (e.g., MDN, freeCodeCamp, Stepik, official docs, trusted YouTube).\n" +
      "Respect time limits: e.g., if user has 4h/week — max 2-4 small tasks or 1 medium task.\n" +
      "Address fears explicitly (e.g., if user fears CSS — start with visual tools or pre-built styles).\n" +
      "Final week must include a capstone project as the main task.\n" +
      " Do NOT use markdown, lists, or line breaks inside a week.\n" +
      "Do NOT add any text before week1 or after the last week." +
      "Now, here is the CourseRequirements input:\n" + request + ".Write the answer in Russian!";
  }
}