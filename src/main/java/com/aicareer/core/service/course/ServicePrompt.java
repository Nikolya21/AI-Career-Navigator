package com.aicareer.core.service.course;

import com.aicareer.core.dto.courseDto.CourseRequest;
import com.aicareer.core.validator.SyntaxValidator;
import com.aicareer.repository.course.PromptGenerator;

public class ServicePrompt implements PromptGenerator {

  @Override
  public String generatePrompt(CourseRequest request) {
    // if (!SyntaxValidator.validate(request)) {
    //     throw new IllegalArgumentException("Validation failed. Cannot generate prompt.");
    // }

    return "СОЗДАЙ УЧЕБНЫЙ ПЛАН ИЗ 8 НЕДЕЛЬ СТРОГО В ЭТОМ ФОРМАТЕ:\n\n" +

        "week1: goal:\"Изучение основ\" task1:\"Практическое задание 1\" urls:\"https://example.com/week1\" task2:\"Теоретическое изучение\" urls:\"https://example.com/theory1\"\n" +
        "week2: goal:\"Продвинутые темы\" task1:\"Проектная работа\" urls:\"https://example.com/project1\" task2:\"Анализ кейсов\" urls:\"https://example.com/cases1\"\n" +
        "week3: goal:\"Применение на практике\" task1:\"Решение задач\" urls:\"https://example.com/tasks1\" task2:\"Создание портфолио\" urls:\"https://example.com/portfolio1\"\n\n" +

        "ПРАВИЛА ФОРМАТА:\n" +
        "1. Начинай каждую неделю с 'weekX:' где X - номер\n" +
        "2. Обязательно указывай goal, task1 и urls для каждой недели\n" +
        "3. На каждую неделю 2 задачи (task1 и task2)\n" +
        "4. URLs могут быть пустыми: urls:\"\"\n" +
        "5. Только 8 недель\n" +
        "6. НИКАКОГО другого текста - только строки в указанном формате\n\n" +

        "ОСНОВА ДЛЯ КУРСА:\n" +
        request.getCourseRequirements() + "\n\n" +

        "ВАЖНО: Если не соблюдешь формат - система не сможет обработать ответ!";
  }
}