package com.aicareer.core.validator;

import com.aicareer.core.annotation.Syntax;
import com.aicareer.core.dto.courseDto.CourseRequest;

import java.lang.reflect.Field;

public class SyntaxValidator {
  public static boolean validate(CourseRequest request) {
    if (request == null || request.getCourseRequirements() == null) {
      System.out.println("CourseRequest or courseRequirements is null");
      return false;
    }

    try {
      // Получаем объект courseRequirements
      Object courseReqsObj = request.getCourseRequirements();

      // Ищем поле name в классе courseReqsObj
      Field nameField = null;
      Class<?> currentClass = courseReqsObj.getClass();

      // Ищем поле в текущем классе и его родителях
      while (currentClass != null && nameField == null) {
        try {
          nameField = currentClass.getDeclaredField("name");
        } catch (NoSuchFieldException e) {
          currentClass = currentClass.getSuperclass();
        }
      }

      if (nameField == null) {
        System.out.println("Поле 'name' не найдено в классе: " + courseReqsObj.getClass().getName());
        return false;
      }

      nameField.setAccessible(true);

      // Проверяем аннотацию @Syntax
      if (!nameField.isAnnotationPresent(Syntax.class)) {
        System.out.println("Поле 'name' не помечено аннотацией @Syntax");
        return false;
      }

      // Получаем значение
      Object value = nameField.get(courseReqsObj);
      if (value == null) {
        System.out.println("Поле 'name' не должно быть null");
        return false;
      }

      String name = value.toString().trim();
      if (name.isEmpty()) {
        System.out.println("Поле 'name' не должно быть пустым");
        return false;
      }

      // Проверяем синтаксис
      if (!name.matches("^[a-zA-Zа-яА-ЯёЁ0-9\\-, :]+$")) {
        Syntax annotation = nameField.getAnnotation(Syntax.class);
        System.out.println(annotation.message());
        return false;
      }

      return true;

    } catch (IllegalAccessException e) {
      System.out.println("Ошибка доступа к полю: " + e.getMessage());
      return false;
    } catch (Exception e) {
      System.out.println("Неожиданная ошибка при валидации: " + e.getMessage());
      return false;
    }
  }
}