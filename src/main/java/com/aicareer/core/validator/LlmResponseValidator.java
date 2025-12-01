package com.aicareer.core.validator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LlmResponseValidator {

  private static final Set<String> TRUSTED_DOMAINS = Set.of(
    "freecodecamp.org",
    "mdn.mozilla.org",
    "stepik.org",
    "coursera.org",
    "javarush.ru",
    "learnjavaonline.org",
    "youtube.com",
    "youtu.be",
    "habr.com",
    "metanit.com",
    "developer.mozilla.org",
    "html5book.ru",
    "webref.ru",
    "css-tricks.com",
    "learnhtmlcss.ru"
  );

  private static final Pattern WEEK_LINE_PATTERN = Pattern.compile("^week(\\d+):\\s*(.+)$");

  public static boolean validate(String llmResponse) {
    if (llmResponse == null || llmResponse.trim().isEmpty()) {
      System.err.println("Ответ от нейросети пуст");
      return false;
    }

    String[] lines = llmResponse.trim().split("\\r?\\n");
    List<String> weekLines = new ArrayList<>();

    for (String line : lines) {
      line = line.trim();
      if (line.isEmpty()) continue;

      Matcher weekMatcher = WEEK_LINE_PATTERN.matcher(line);
      if (weekMatcher.matches()) {
        weekLines.add(line);
      }
    }

    if (weekLines.size() < 4 && !weekLines.isEmpty()) {
      System.err.println("План должен содержать минимум 4 недели, получено: " + weekLines.size());
      return false;
    }

    for (String weekLine : weekLines) {
      if (!isValidWeekLine(weekLine)) {
        return false;
      }
    }

    return true;
  }

  private static boolean isValidWeekLine(String weekLine) {
    String content = weekLine.substring(weekLine.indexOf(':') + 1).trim();
    String[] parts = content.split("\\.\\s*(?=\\w+:)");

    Map<String, String> fields = new LinkedHashMap<>();
    for (String part : parts) {
      int colonIndex = part.indexOf(':');
      if (colonIndex == -1) {
        System.err.println("Некорректная часть без двоеточия: " + part);
        return false;
      }
      String key = part.substring(0, colonIndex).trim();
      String value = part.substring(colonIndex + 1).trim();
      if (value.startsWith("\"") && value.endsWith("\"")) {
        value = value.substring(1, value.length() - 1).trim();
      }
      fields.put(key.toLowerCase(), value);
    }

    if (!fields.containsKey("goal") || fields.get("goal").isEmpty()) {
      System.err.println("Отсутствует или пустое поле 'goal'");
      return false;
    }
    int taskCount = 0;
    while (fields.containsKey("task" + (taskCount + 1))) {
      taskCount++;
      String taskKey = "task" + taskCount;
      String urlKey = "urls";
    }
    return validateBySequentialParsing(parts);
  }
  private static boolean validateBySequentialParsing(String[] parts) {
    boolean hasGoal = false;
    int expectedTaskNumber = 1;
    boolean expectingUrlsAfterTask = false;

    for (String part : parts) {
      int colonIndex = part.indexOf(':');
      if (colonIndex == -1) {
        System.err.println("Некорректный сегмент без ':': " + part);
        return false;
      }

      String key = part.substring(0, colonIndex).trim().toLowerCase();
      String value = part.substring(colonIndex + 1).trim();
      if (value.startsWith("\"") && value.endsWith("\"")) {
        value = value.substring(1, value.length() - 1).trim();
      }

      if (key.equals("goal")) {
        if (hasGoal) {
          System.err.println("Дублирующееся поле 'goal'");
          return false;
        }
        if (value.isEmpty()) {
          System.err.println("Поле 'goal' пустое");
          return false;
        }
        hasGoal = true;
      }
      else if (key.startsWith("task")) {
        if (!hasGoal) {
          System.err.println("Поле задачи до 'goal'");
          return false;
        }
        String taskNumberPart = key.substring(4);
        if (!taskNumberPart.matches("\\d+")) {
          System.err.println("Некорректное имя задачи: " + key);
          return false;
        }
        int taskNumber = Integer.parseInt(taskNumberPart);
        if (taskNumber != expectedTaskNumber) {
          System.err.println("Нарушен порядок задач: ожидалась task" + expectedTaskNumber + ", получена " + key);
          return false;
        }
        if (value.isEmpty()) {
          System.err.println("Пустая задача: " + key);
          return false;
        }
        expectedTaskNumber++;
        expectingUrlsAfterTask = true;
      }
      else if (key.equals("urls")) {
        if (!expectingUrlsAfterTask) {
          System.err.println("Поле 'urls' без предшествующей задачи");
          return false;
        }
        if (value.isEmpty()) {
          System.err.println("Пустое поле 'urls'");
          return false;
        }
        String[] urls = value.split(",");
        boolean hasValidUrl = false;
        for (String url : urls) {
          url = url.trim();
          if (url.isEmpty()) continue;
          if (!isTrustedUrl(url)) {
            System.err.println("Недоверенный URL: " + url);
            return false;
          }
          hasValidUrl = true;
        }
        if (!hasValidUrl) {
          System.err.println("Ни одного валидного URL в 'urls'");
          return false;
        }
        expectingUrlsAfterTask = false;
      }
      else {
        System.err.println("Неизвестное поле: " + key);
        return false;
      }
    }

    if (!hasGoal) {
      System.err.println("Отсутствует 'goal'");
      return false;
    }
    if (expectingUrlsAfterTask) {
      System.err.println("Ожидалось поле 'urls' после последней задачи");
      return false;
    }

    return true;
  }
  private static boolean isTrustedUrl(String url) {
    try {
      URI uri = new URI(url);
      String host = uri.getHost();
      if (host == null) return false;
      host = host.toLowerCase().replaceFirst("^www\\.", "");
      return TRUSTED_DOMAINS.contains(host);
    } catch (URISyntaxException e) {
      return false;
    }
  }
}