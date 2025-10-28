package com.aicareer.core.Validator;

import com.aicareer.core.Annotation.Syntax;
import com.aicareer.core.DTO.CourseRequest;

import java.lang.reflect.Field;

public class SyntaxValidator {
  public static boolean validate(CourseRequest request) {
    if (request == null || request.getCourseRequirements() == null) {
      return false;
    }
    try {
      Field courseReqsField = CourseRequest.class.getDeclaredField("courseRequirements");
      courseReqsField.setAccessible(true);
      Object courseReqsObj = courseReqsField.get(request);
      if (courseReqsObj == null) {
        System.out.println("courseRequirements must not be null");
        return false;
      }
      Field nameField = courseReqsObj.getClass().getDeclaredField("name");
      nameField.setAccessible(true);
      if(!nameField.isAnnotationPresent(Syntax.class)) {
        System.out.println("Поле 'name' не помечено аннотацией @OnlyLetters");
        return false;
      }
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
      if (!name.matches("^[a-zA-Zа-яА-ЯёЁ0-9\\-, :]+$")) {
        Syntax annotation = nameField.getAnnotation(Syntax.class);
        System.out.println(annotation.message());
        return false;
      }
      return true;
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}