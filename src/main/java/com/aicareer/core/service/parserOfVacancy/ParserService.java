package com.aicareer.core.service.parserOfVacancy;

import com.aicareer.core.model.vacancy.RealVacancy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.aicareer.core.service.gigachat.GigaChatService;
import org.json.JSONArray;
import org.json.JSONObject;

public class ParserService {

  private static final GigaChatService gigaChatService = new GigaChatService();

    public static List<RealVacancy> getVacancies(String searchText, String area, int perPage) {
    List<RealVacancy> vacancies = new ArrayList<>();
    try {
      String apiUrl = String.format(
          "https://api.hh.ru/vacancies?text=%s&area=%s&per_page=%d",
          searchText.replace(" ", "+"),
          area,
          perPage
      );
      String jsonResponse = sendGetRequest(apiUrl);
      JSONObject jsonObject = new JSONObject(jsonResponse);
      JSONArray items = jsonObject.getJSONArray("items");
      for (int i = 0; i < items.length(); i++) {
        JSONObject item = items.getJSONObject(i);

        String id = item.getString("id");

        String title = item.getString("name");

        String salary = parseSalary(item.optJSONObject("salary"));

        String experience = item.getJSONObject("experience").getString("name");

        List<String> keySkills = getKeySkillsForVacancy(item.optJSONObject("key_skills"));

        vacancies.add(new RealVacancy(title,  keySkills, salary));
      }
    } catch (Exception e) {
      System.err.println("Ошибка при получении" + e.getMessage());
    }
      System.out.println(vacancies);
      // FALLBACK ЛОГИКА
      if (vacancies.isEmpty()) {
        vacancies = getVacanciesWithFallback(searchText, area, perPage); //todo развлекайся, братанкчик:)
      }

      return vacancies;
    }

  private static List<RealVacancy> getVacanciesWithFallback(String searchText, String area, int perPage) { //todo сделай, чтобы работало - я в тебя верю :)
    List<RealVacancy> vacancies = new ArrayList<>();

    System.out.println("Вакансия '" + searchText + "' не найдена. Ищу альтернативные названия...");

    // Получаем и валидируем альтернативные ключевые слова
    List<String> alternativeTitles = getValidatedAlternativeKeywords(searchText, area);

    // Пробуем каждый альтернативный вариант
    for (String alternative : alternativeTitles) {
      String trimmedAlternative = alternative.trim();
      System.out.println("Пробую альтернативу: " + trimmedAlternative);

      try {
        List<RealVacancy> alternativeVacancies = parseVacanciesForKeyword(trimmedAlternative, area, perPage);

        if (!alternativeVacancies.isEmpty()) {
          System.out.println("Найдено " + alternativeVacancies.size() + " вакансий по альтернативе: " + trimmedAlternative);
          vacancies.addAll(alternativeVacancies);
          break; // Выходим после первой успешной альтернативы
        }
      } catch (Exception e) {
        System.err.println("Ошибка при поиске по альтернативе '" + trimmedAlternative + "': " + e.getMessage());
      }
    }

    return vacancies;
  }

  private static List<String> getValidatedAlternativeKeywords(String searchText, String area) {
    try {
      // Получаем альтернативы от нейронки
      String alternatives = findAlternativeKeywords(searchText, "поиск на hh.ru в области " + area);

      // Валидируем и исправляем формат
      String validatedResult = validateAndFixKeywordsFormat(alternatives, searchText);

      // Парсим в список
      return parseKeywords(validatedResult);

    } catch (Exception e) {
      System.err.println("Ошибка при получении альтернативных ключевых слов: " + e.getMessage());
      return getDefaultAlternativeKeywords(searchText);
    }
  }

  private static List<RealVacancy> parseVacanciesForKeyword(String keyword, String area, int perPage) {
    List<RealVacancy> vacancies = new ArrayList<>();

    try {
      String apiUrl = String.format(
              "https://api.hh.ru/vacancies?text=%s&area=%s&per_page=%d",
              keyword.replace(" ", "+"),
              area,
              perPage
      );
      String jsonResponse = sendGetRequest(apiUrl);
      JSONObject jsonObject = new JSONObject(jsonResponse);
      JSONArray items = jsonObject.getJSONArray("items");

      for (int i = 0; i < items.length(); i++) {
        JSONObject item = items.getJSONObject(i);
        String id = item.getString("id");
        String title = item.getString("name");
        String salary = parseSalary(item.optJSONObject("salary"));
        String experience = item.getJSONObject("experience").getString("name");
        List<String> keySkills = getKeySkillsForVacancy(item.optJSONObject("key_skills"));

        vacancies.add(new RealVacancy(title, keySkills, salary));
      }
    } catch (Exception e) {
      throw new RuntimeException("Ошибка парсинга для ключевого слова: " + keyword, e);
    }

    return vacancies;
  }

  private static List<String> parseKeywords(String keywordsString) {
    List<String> keywords = new ArrayList<>();
    String[] parts = keywordsString.split(",");

    for (String part : parts) {
      String trimmed = part.trim();
      if (!trimmed.isEmpty()) {
        keywords.add(trimmed);
      }
    }

    // Обеспечиваем минимум 3 элемента
    while (keywords.size() < 3) {
      keywords.add("Резервная вакансия");
    }

    return keywords.subList(0, Math.min(3, keywords.size()));
  }

  private static List<String> getDefaultAlternativeKeywords(String searchText) {
    // Дефолтные альтернативы на случай ошибок
    return List.of(
            searchText + " разработчик",
            searchText + " программист",
            searchText + " engineer"
    );
  }

  public static String findAlternativeKeywords(String originalVacancy, String context) {
    String prompt = """
        СРОЧНАЯ ЗАДАЧА: Найти альтернативные названия вакансии (исходный запрос не дал результатов)
        
        КОНТЕКСТ ПРОБЛЕМЫ:
        - Поиск вакансии "%s" вернул 0 результатов
        - Контекст: %s
        - Нужно найти альтернативные названия этой же должности
        
        ЭКСПЕРТНАЯ ЗАДАЧА:
        Проанализируй должность "%s" и предложи 3 альтернативных названия, которые:
        
        1. **Означают ту же самую должность** - не смежную профессию
        2. **Часто используются в реальных вакансиях** - популярные HR-формулировки
        3. **Включают разные варианты написания**:
           - Синонимичные названия
           - Более формальные/неформальные варианты
           - С указанием технологий или без
           - Английские/русские варианты названия
           - С разным порядком слов
        
        ПРИМЕРЫ:
        - "Java разработчик" → "Java программист, Разработчик Java, Java Developer"
        - "Data Scientist" → "Data Scientist, Специалист по данным, Data Science Engineer"
        - "Менеджер проектов" → "Project Manager, PM, Руководитель проектов"
        - "Frontend разработчик" → "Frontend developer, Front-end программист, Веб-разработчик"
        
        ТРЕБОВАНИЯ К ФОРМАТУ:
        - Ровно 3 альтернативных названия вакансии через запятую
        - Без лишних символов, только названия должностей
        - Каждое название должно быть полноценной вакансией
        
        ВАЖНО: Ответ должен содержать ТОЛЬКО 3 названия вакансий через запятую, без пояснений!
        
        Исходная вакансия: "%s"
        """;

    return gigaChatService.sendMessage(String.format(prompt, originalVacancy, context, originalVacancy, originalVacancy));
  }

  private static String validateAndFixKeywordsFormat(String rawResponse, String originalVacancy) {
    // Сначала проверяем простыми методами
    if (isValidKeywordsFormat(rawResponse)) {
      return rawResponse;
    }

    // Если формат неправильный - исправляем через нейронку
    String validationPrompt = """
        КРИТИЧЕСКАЯ ЗАДАЧА: Исправить формат списка вакансий
        
        ОШИБКА ФОРМАТА:
        Получен некорректный ответ: "%s"
        
        ТРЕБУЕМЫЙ ФОРМАТ:
        - Ровно 3 названия вакансий через запятую
        - Без номеров, без точек, без пояснений
        - Формат: "Вакансия1, Вакансия2, Вакансия3"
        
        ПРАВИЛА ИСПРАВЛЕНИЯ:
        1. Если элементов больше 3 - оставь первые 3
        2. Если элементов меньше 3 - дополни список синонимами исходной вакансии "%s"
        3. Удали все номера (1., 2., 3.)
        4. Удали все точки в конце
        5. Удали все пояснения и комментарии
        6. Убедись, что все 3 элемента - это названия вакансий (должностей)
        
        ПРИМЕРЫ ПРАВИЛЬНОГО ФОРМАТА:
        - "Java разработчик, Java программист, Java Developer"
        - "Data Scientist, Специалист по данным, Data Analyst"
        - "Frontend разработчик, Front-end developer, Веб-разработчик"
        
        ВАЖНО: Верни ТОЛЬКО исправленную строку в правильном формате!
        """;

    return gigaChatService.sendMessage(String.format(validationPrompt, rawResponse, originalVacancy));
  }

  // Метод проверки формата
  private static boolean isValidKeywordsFormat(String response) {
    if (response == null || response.trim().isEmpty()) {
      return false;
    }

    String trimmed = response.trim();

    // Проверяем, что есть запятые (минимум 2 для 3 элементов)
    int commaCount = countCharacters(trimmed, ',');
    if (commaCount < 2) {
      return false;
    }

    // Проверяем, что нет номеров (1., 2., 3.)
    if (trimmed.matches(".*\\d+\\..*")) {
      return false;
    }

    // Проверяем, что нет лишних символов (двоеточия, тире в начале и т.д.)
    if (trimmed.startsWith(":") || trimmed.startsWith("-") || trimmed.startsWith("•")) {
      return false;
    }

    // Разбиваем на элементы и проверяем количество
    String[] keywords = trimmed.split(",");
    if (keywords.length != 3) {
      return false;
    }

    // Проверяем, что каждый элемент не пустой
    for (String keyword : keywords) {
      if (keyword.trim().isEmpty()) {
        return false;
      }
    }

    return true;
  }

  // Вспомогательный метод для подсчета символов
  private static int countCharacters(String str, char ch) {
    int count = 0;
    for (char c : str.toCharArray()) {
      if (c == ch) count++;
    }
    return count;
  }


  private static List<String> getKeySkillsForVacancy(JSONObject skillsObj) {
    List<String> skills = new ArrayList<>();
    if (skillsObj == null){
      return null;
    }
    try {
      if (skillsObj.has("key_skills")) {
        JSONArray keySkillsArray = skillsObj.getJSONArray("key_skills");

        for (int i = 0; i < keySkillsArray.length(); i++) {
          JSONObject skill = keySkillsArray.getJSONObject(i);
          skills.add(skill.getString("name"));
        }
      }
    } catch (Exception e) {}

    return skills;
  }

  private static String parseSalary(JSONObject salaryObj) {
    if (salaryObj == null) {
      return null;
    }
    try {
      String from = salaryObj.has("from") ? String.valueOf(salaryObj.getInt("from")) : null;
      String to = salaryObj.has("to") ? String.valueOf(salaryObj.getInt("to")) : null;
      String currency = salaryObj.has("currency") ? salaryObj.getString("currency") : "";

      if (from != null && to != null) {
        return from + " - " + to + " " + currency;
      } else if (from != null) {
        return "от " + from + " " + currency;
      } else if (to != null) {
        return "до " + to + " " + currency;
      }

    } catch (Exception e) {
    }
    return null;
  }
  private static String sendGetRequest(String urlString) throws Exception {
    URL url = new URL(urlString);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

    connection.setRequestMethod("GET");
    connection.setRequestProperty("User-Agent", "HH-Parser/1.0");
    connection.setRequestProperty("Accept", "application/json");

    int responseCode = connection.getResponseCode();

    if (responseCode == HttpURLConnection.HTTP_OK) {
      String inputLine;
      StringBuilder response = new StringBuilder();

      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();
      return response.toString();
    } else {
      throw new RuntimeException("HTTP error: " + responseCode);
    }

    }
}