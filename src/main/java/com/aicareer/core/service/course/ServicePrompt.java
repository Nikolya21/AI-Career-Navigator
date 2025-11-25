package com.aicareer.core.service.course;

import com.aicareer.core.dto.courseDto.CourseRequest;
import com.aicareer.repository.course.PromptGenerator;

public class ServicePrompt implements PromptGenerator {

  @Override
  public String generatePrompt(CourseRequest request) {
    String topic = request.getCourseRequirements();

    return "СТРОГИЙ ФОРМАТ! НЕ ОТКЛОНЯЙСЯ НИ НА СИМВОЛ!\n\n" +

        "Создай учебный план из 8 недель по теме: " + topic + "\n\n" +

        "ОБЯЗАТЕЛЬНЫЕ ТРЕБОВАНИЯ:\n" +
        "1. КАЖДАЯ задача должна иметь МИНИМУМ 1 реальную ссылку\n" +
        "2. Ссылки должны быть релевантными теме\n" +
        "3. Используй реальные образовательные ресурсы\n" +
        "4. Запрещено оставлять URLs пустыми\n\n" +

        "ФОРМАТ (ПОВТОРИ 8 РАЗ ТОЧНО):\n" +
        "===WEEK_START===\n" +
        "NUMBER:[1-8]\n" +
        "GOAL:[цель недели 15-50 слов]\n" +
        "===TASK_START===\n" +
        "DESCRIPTION:[задача 1: 10-30 слов]\n" +
        "URLS:[обязательно 1-3 реальных URL через запятую]\n" +
        "===TASK_END===\n" +
        "===TASK_START===\n" +
        "DESCRIPTION:[задача 2: 10-30 слов]\n" +
        "URLS:[обязательно 1-3 реальных URL через запятую]\n" +
        "===TASK_END===\n" +
        "===WEEK_END===\n\n" +

        "РЕАЛЬНЫЕ ОБРАЗОВАТЕЛЬНЫЕ РЕСУРСЫ ДЛЯ ССЫЛОК:\n" +
        "- Coursera, edX, Udemy, Stepik, Khan Academy\n" +
        "- YouTube каналы: freeCodeCamp, CS50, Traversy Media\n" +
        "- Официальная документация (MDN, Microsoft Learn, Oracle)\n" +
        "- GitHub репозитории, Stack Overflow, Habr\n" +
        "- Интерактивные платформы: Codecademy, LeetCode, HackerRank\n\n" +

        "ПРИМЕР ДЛЯ ТЕМЫ 'ПРОГРАММИРОВАНИЕ':\n" +
        "===WEEK_START===\n" +
        "NUMBER:1\n" +
        "GOAL:Изучение основ программирования на Python, включая переменные, типы данных и базовые операции\n" +
        "===TASK_START===\n" +
        "DESCRIPTION:Установить Python и настроить среду разработки. Написать первую программу 'Hello World'\n" +
        "URLS:https://www.python.org/downloads/,https://code.visualstudio.com/docs/python/python-tutorial,https://www.learnpython.org/\n" +
        "===TASK_END===\n" +
        "===TASK_START===\n" +
        "DESCRIPTION:Изучить переменные, типы данных и основные операторы. Решить 10 практических задач\n" +
        "URLS:https://www.w3schools.com/python/python_variables.asp,https://www.programiz.com/python-programming/variables-datatypes\n" +
        "===TASK_END===\n" +
        "===WEEK_END===\n\n" +

        "ТЕМА ДЛЯ КУРСА: " + topic + "\n\n" +

        "ВЫВЕДИ ТОЧНО 8 НЕДЕЛЬ В ЭТОМ ФОРМАТЕ! НИКАКИХ ОТСТУПЛЕНИЙ!\n" +
        "КАЖДАЯ ЗАДАЧА ДОЛЖНА ИМЕТЬ РЕАЛЬНЫЕ ССЫЛКИ!";
  }
}