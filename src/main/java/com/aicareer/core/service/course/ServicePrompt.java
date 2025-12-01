package com.aicareer.core.service.course;

import com.aicareer.core.dto.courseDto.CourseRequest;
import com.aicareer.repository.course.PromptGenerator;

public class ServicePrompt implements PromptGenerator {

  @Override
  public String generatePrompt(CourseRequest request) {
    String topic = request.getCourseRequirements();

    return """
STRICT FORMAT ONLY! DO NOT DEVIATE BY A SINGLE CHARACTER!

Generate an 8-12-weeks learning plan on the topic: """ + topic + """

MANDATORY REQUIREMENTS:
1. EACH task MUST have at least 3 REAL educational RESOURCES (NOT URLs)
2. Resources must be in Russian with proper formatting
3. NEVER use URLs or web links
4. NEVER leave RESOURCES empty

FORMAT (REPEAT EXACTLY 8 TIMES):
===WEEK_START===
NUMBER:[1-8]
GOAL:[week goal in Russian, 15-50 words]
===TASK_START===
DESCRIPTION:[task 1 in Russian: 10-30 words]
RESOURCES:[resource1], [resource2], [resource3]
===TASK_END===
===TASK_START===
DESCRIPTION:[task 2 in Russian: 10-30 words]
RESOURCES:[resource1], [resource2]
===TASK_END===
===WEEK_END===

RESOURCE FORMATS (MUST FOLLOW EXACTLY):
- Книга «Название книги» автор И. И. Иванов (главы X-Y)
- Статья «Название статьи» на Habr.ru
- Видео «Название видео» на Rutube канал «Название канала»
- Курс «Название курса» на Coursera
- Документация «Название технологии» (раздел X)
- Практика «Название задания» на Kaggle.com

EXAMPLE FOR TOPIC 'PYTHON':
===WEEK_START===
NUMBER:1
GOAL:Освоить базовый синтаксис Python и настроить среду разработки
===TASK_START===
DESCRIPTION:Установить Python и изучить базовые команды языка
RESOURCES:Книга «Python. К вершинам мастерства» автор Лучано Рамальо (главы 1-2), Видео «Установка и настройка Python» на Rutube канал «ITVDN», Статья «Первые шаги в Python» на Хабр.ru
===TASK_END===
===TASK_START===
DESCRIPTION:Изучить переменные, типы данных и основные операторы
RESOURCES:Книга «Автоматизация рутины с помощью Python» автор Эл Свейгарт (главы 3-4), Курс «Основы программирования на Python» на Stepik.org
===TASK_END===
===WEEK_END===

TOPIC FOR COURSE: """ + topic + """

OUTPUT EXACTLY 8-12 WEEKS IN THIS FORMAT! NO DEVIATIONS!
NEVER USE URLS OR WEB LINKS! ONLY USE RESOURCE NAMES IN SPECIFIED FORMAT!
OUTPUT ONLY THE STRUCTURED CONTENT. NO INTRODUCTIONS. NO APOLOGIES. NO EXPLANATIONS.
""";
  }
}