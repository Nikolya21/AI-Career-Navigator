// com.aicareer.core.service.course.ServiceWeek
package com.aicareer.core.service.course;

import com.aicareer.core.model.Task;
import com.aicareer.core.model.Week;
import com.aicareer.module.course.CourseResponse;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceWeek implements CourseResponse {

  private static final Pattern WEEK_LINE_PATTERN = Pattern.compile("^week(\\d+):\\s*(.+)$");
  private static final Pattern FIELD_PATTERN = Pattern.compile("(\\w+)\\s*:\\s*\"([^\"]*)\"");

  @Override
  public List<Week> parseCourseResponse(String llmResponse) {
    if (llmResponse == null || llmResponse.trim().isEmpty()) {
      return List.of();
    }

    List<Week> weeks = new ArrayList<>();
    String[] lines = llmResponse.trim().split("\\r?\\n");

    for (String line : lines) {
      line = line.trim();
      if (line.isEmpty()) continue;

      Matcher weekMatcher = WEEK_LINE_PATTERN.matcher(line);
      if (!weekMatcher.matches()) continue;

      int weekNumber = Integer.parseInt(weekMatcher.group(1));
      String content = weekMatcher.group(2);

      Week week = parseWeekContent(weekNumber, content);
      if (week != null) {
        weeks.add(week);
      }
    }

    weeks.sort(Comparator.comparingInt(Week::getNumber));
    return weeks;
  }

  private Week parseWeekContent(int weekNumber, String content) {
    if (content.endsWith(".")) {
      content = content.substring(0, content.length() - 1);
    }

    String[] segments = content.split("\\.\\s*(?=\\w+:)");

    String goal = null;
    List<Task> tasks = new ArrayList<>();
    String currentTaskDescription = null;

    for (String segment : segments) {
      segment = segment.trim();
      if (segment.isEmpty()) continue;
      Matcher fieldMatcher = FIELD_PATTERN.matcher(segment);
      if (fieldMatcher.find()) {
        String key = fieldMatcher.group(1).toLowerCase();
        String value = fieldMatcher.group(2);
        switch (key) {
          case "goal":
            goal = value;
            break;
          case "urls":
            if (currentTaskDescription != null && !currentTaskDescription.isEmpty()) {
              List<String> urls = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
              tasks.add(new Task(currentTaskDescription, urls));
              currentTaskDescription = null;
            }
            break;
          default:
            if (key.startsWith("task")) {
              if (currentTaskDescription != null && !currentTaskDescription.isEmpty()) {
                tasks.add(new Task(currentTaskDescription, List.of()));
              }
              currentTaskDescription = value;
            }
            break;
        }
      } else {
        int colonIndex = segment.indexOf(':');
        if (colonIndex == -1) continue;

        String key = segment.substring(0, colonIndex).trim().toLowerCase();
        String value = segment.substring(colonIndex + 1).trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
          value = value.substring(1, value.length() - 1);
        }
        switch (key) {
          case "goal":
            goal = value;
            break;
          case "urls":
            if (currentTaskDescription != null && !currentTaskDescription.isEmpty()) {
              List<String> urls = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
              tasks.add(new Task(currentTaskDescription, urls));
              currentTaskDescription = null;
            }
            break;
          default:
            if (key.startsWith("task")) {
              if (currentTaskDescription != null && !currentTaskDescription.isEmpty()) {
                tasks.add(new Task(currentTaskDescription, List.of()));
              }
              currentTaskDescription = value;
            }
            break;
        }
      }
    }
    if (currentTaskDescription != null && !currentTaskDescription.isEmpty()) {
      tasks.add(new Task(currentTaskDescription, List.of()));
    }

    if (goal == null || goal.isEmpty()) {
      return null;
    }

    return new Week(weekNumber, goal, tasks);
  }
}