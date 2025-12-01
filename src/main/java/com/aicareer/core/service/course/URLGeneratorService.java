package com.aicareer.core.service.course;

import com.aicareer.core.validator.URLValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class URLGeneratorService {
  private final URLValidator urlValidator;

  public URLGeneratorService() {
    this.urlValidator = new URLValidator();
  }

  /**
   * Генерирует реальные рабочие URL (максимум 3)
   */
  public List<String> getValidUrlsForTopic(String topic, String taskDescription) {
    System.out.println("🔄 Поиск рабочих URL для: " + taskDescription);

    // 🔥 ИСПОЛЬЗУЕМ ТОЛЬКО ПРОВЕРЕННЫЕ РЕАЛЬНЫЕ URL
    List<String> realUrls = generateRealUrls(topic, taskDescription);

    // Проверяем какие из них работают (максимум 3)
    List<String> validUrls = urlValidator.filterValidUrls(realUrls);

    // 🔥 ЕСЛИ НЕТ РАБОЧИХ - ВОЗВРАЩАЕМ ГАРАНТИРОВАННО РАБОЧИЕ
    if (validUrls.isEmpty()) {
      System.out.println("⚠️ Нет рабочих URL, используем гарантированные");
      validUrls = getGuaranteedWorkingUrls();
    }

    System.out.println("✅ Найдено рабочих URL: " + validUrls.size());
    return validUrls;
  }

  /**
   * Генерирует только реальные URL
   */
  private List<String> generateRealUrls(String topic, String taskDescription) {
    List<String> urls = new ArrayList<>();

    // 🔥 ТОЛЬКО РЕАЛЬНЫЕ ПРОВЕРЕННЫЕ САЙТЫ С КОНКРЕТНЫМИ СТРАНИЦАМИ
    String[] guaranteedUrls = {
      // Python & Programming
      "https://docs.python.org/3/tutorial/",
      "https://www.w3schools.com/python/",
      "https://realpython.com/",
      "https://www.learnpython.org/",
      "https://www.programiz.com/python-programming",

      // SQL & Databases
      "https://www.w3schools.com/sql/",
      "https://www.tutorialspoint.com/sql/",
      "https://www.sqlite.org/docs.html",
      "https://www.postgresql.org/docs/",

      // Data Science & Analytics
      "https://pandas.pydata.org/docs/",
      "https://numpy.org/doc/",
      "https://matplotlib.org/stable/contents.html",
      "https://seaborn.pydata.org/tutorial.html",

      // Web Development
      "https://developer.mozilla.org/en-US/docs/Web",
      "https://www.w3.org/TR/",
      "https://web.dev/learn/",

      // Tools & Platforms
      "https://powerbi.microsoft.com/en-us/documentation/",
      "https://developers.google.com/analytics",
      "https://ads.google.com/home/",
      "https://analytics.google.com/analytics/academy/",

      // Educational Platforms
      "https://www.coursera.org/learn",
      "https://www.edx.org/learn",
      "https://www.khanacademy.org/computing",
      "https://www.freecodecamp.org/learn",
      "https://www.codecademy.com/learn",

      // Documentation
      "https://dev.mysql.com/doc/",
      "https://docs.microsoft.com/en-us/sql/",
      "https://www.postgresql.org/docs/current/",

      // YouTube Tutorials
      "https://www.youtube.com/watch?v=rfscVS0vtbw", // Python tutorial
      "https://www.youtube.com/watch?v=HXV3zeQKqGY", // SQL tutorial
      "https://www.youtube.com/watch?v=UB1O30fR-EE", // HTML/CSS
    };

    urls.addAll(Arrays.asList(guaranteedUrls));

    // Добавляем тематические URL в зависимости от темы
    addTopicSpecificUrls(urls, topic.toLowerCase());

    return urls;
  }

  private void addTopicSpecificUrls(List<String> urls, String topic) {
    if (topic.contains("python") || topic.contains("программир")) {
      urls.addAll(Arrays.asList(
        "https://docs.python.org/3/tutorial/introduction.html",
        "https://www.w3schools.com/python/python_getstarted.asp",
        "https://realpython.com/python-first-steps/"
      ));
    }

    if (topic.contains("sql") || topic.contains("баз")) {
      urls.addAll(Arrays.asList(
        "https://www.w3schools.com/sql/sql_intro.asp",
        "https://www.tutorialspoint.com/sql/sql-overview.htm",
        "https://www.sqlite.org/lang.html"
      ));
    }

    if (topic.contains("аналитик") || topic.contains("data")) {
      urls.addAll(Arrays.asList(
        "https://pandas.pydata.org/docs/getting_started/index.html",
        "https://numpy.org/doc/stable/user/absolute_beginners.html",
        "https://matplotlib.org/stable/users/index.html"
      ));
    }

    if (topic.contains("маркетинг") || topic.contains("marketing")) {
      urls.addAll(Arrays.asList(
        "https://ads.google.com/home/how-it-works/",
        "https://analytics.google.com/analytics/academy/course/6",
        "https://support.google.com/google-ads/answer/1704395"
      ));
    }
  }

  /**
   * Гарантированно рабочие URL (максимум 3)
   */
  private List<String> getGuaranteedWorkingUrls() {
    return Arrays.asList(
      "https://www.google.com",
      "https://www.youtube.com",
      "https://github.com"
    );
  }
}