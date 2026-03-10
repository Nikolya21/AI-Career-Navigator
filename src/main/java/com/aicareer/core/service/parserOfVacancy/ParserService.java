package com.aicareer.core.service.parserOfVacancy;

import com.aicareer.core.model.vacancy.RealVacancy;
import com.aicareer.core.service.gigachat.GigaChatService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class ParserService {

  private final GigaChatService gigaChatService;

  public ParserService(GigaChatService gigaChatService) {
    this.gigaChatService = gigaChatService;
  }

  public List<RealVacancy> getVacancies(String searchText, String area, int perPage) {
    List<RealVacancy> vacancies = new ArrayList<>();
    try {
      String apiUrl = String.format(
          "https://api.hh.ru/vacancies?text=%s&area=%s&per_page=%d",
          searchText.replace(" ", "+"),
          area,
          perPage
      );

      System.out.println("🌐 Запрашиваем API: " + apiUrl);
      String jsonResponse = sendGetRequest(apiUrl);
      JSONObject jsonObject = new JSONObject(jsonResponse);
      JSONArray items = jsonObject.getJSONArray("items");

      System.out.println("📊 Найдено вакансий в API: " + items.length());

      for (int i = 0; i < items.length(); i++) {
        JSONObject item = items.getJSONObject(i);
        String title = item.getString("name");
        String salary = parseSalary(item.optJSONObject("salary"));
        String experience = item.getJSONObject("experience").getString("name");
        String age = item.optJSONObject("age_restriction") != null ?
            item.getJSONObject("age_restriction").getString("name") : "Не указано";
        String employer = item.getJSONObject("employer").getString("name");

        // Ключевые навыки
        JSONArray keySkillsArray = item.optJSONArray("key_skills");
        List<String> keySkills = new ArrayList<>();
        if (keySkillsArray != null) {
          for (int j = 0; j < keySkillsArray.length(); j++) {
            JSONObject skill = keySkillsArray.getJSONObject(j);
            keySkills.add(skill.getString("name"));
          }
        }

        System.out.println("✅ Вакансия: " + title + ", Навыков: " + keySkills.size());
        vacancies.add(new RealVacancy(title, keySkills, salary, experience, age, employer));
      }
    } catch (Exception e) {
      System.err.println("❌ Ошибка при получении вакансий: " + e.getMessage());
      e.printStackTrace();
    }

    if (vacancies.isEmpty()) {
      System.out.println("⚠️ Основной поиск не дал результатов, запускаем fallback");
      vacancies = getVacanciesWithFallback(searchText, area, perPage);
    }

    System.out.println("🏁 Итоговое количество вакансий: " + vacancies.size());
    return vacancies;
  }

  private List<RealVacancy> getVacanciesWithFallback(String searchText, String area, int perPage) {
    List<RealVacancy> vacancies = new ArrayList<>();
    System.out.println("Вакансия '" + searchText + "' не найдена. Ищу альтернативные названия...");

    List<String> alternativeTitles = getValidatedAlternativeKeywords(searchText, area);
    for (String alternative : alternativeTitles) {
      String trimmedAlternative = alternative.trim();
      System.out.println("Пробую альтернативу: " + trimmedAlternative);
      try {
        List<RealVacancy> alternativeVacancies = parseVacanciesForKeyword(trimmedAlternative, area, perPage);
        if (!alternativeVacancies.isEmpty()) {
          System.out.println("Найдено " + alternativeVacancies.size() + " вакансий по альтернативе: " + trimmedAlternative);
          vacancies.addAll(alternativeVacancies);
          break;
        }
      } catch (Exception e) {
        System.err.println("Ошибка при поиске по альтернативе '" + trimmedAlternative + "': " + e.getMessage());
      }
    }
    return vacancies;
  }

  private List<String> getValidatedAlternativeKeywords(String searchText, String area) {
    try {
      String alternatives = findAlternativeKeywords(searchText, "поиск на hh.ru в области " + area);
      String validatedResult = validateAndFixKeywordsFormat(alternatives, searchText);
      return parseKeywords(validatedResult);
    } catch (Exception e) {
      System.err.println("Ошибка при получении альтернативных ключевых слов: " + e.getMessage());
      return getDefaultAlternativeKeywords(searchText);
    }
  }

  private List<RealVacancy> parseVacanciesForKeyword(String keyword, String area, int perPage) {
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
        String title = item.getString("name");
        String salary = parseSalary(item.optJSONObject("salary"));
        String experience = item.getJSONObject("experience").getString("name");
        String age = item.optJSONObject("age_restriction") != null ?
            item.getJSONObject("age_restriction").getString("name") : "Не указано";
        String employer = item.getJSONObject("employer").getString("name");
        List<String> keySkills = getKeySkillsForVacancy(item.optJSONArray("key_skills"));
        vacancies.add(new RealVacancy(title, keySkills, salary, experience, age, employer));
      }
    } catch (Exception e) {
      throw new RuntimeException("Ошибка парсинга для ключевого слова: " + keyword, e);
    }
    return vacancies;
  }

  private List<String> parseKeywords(String keywordsString) {
    List<String> keywords = new ArrayList<>();
    String[] parts = keywordsString.split(",");
    for (String part : parts) {
      String trimmed = part.trim();
      if (!trimmed.isEmpty()) {
        keywords.add(trimmed);
      }
    }
    while (keywords.size() < 3) {
      keywords.add("Резервная вакансия");
    }
    return keywords.subList(0, Math.min(3, keywords.size()));
  }

  private List<String> getDefaultAlternativeKeywords(String searchText) {
    return List.of(searchText + " developer", searchText + " QA", searchText + " engineer");
  }

  public String findAlternativeKeywords(String originalVacancy, String context) {
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

  private String validateAndFixKeywordsFormat(String rawResponse, String originalVacancy) {
    if (isValidKeywordsFormat(rawResponse)) {
      return rawResponse;
    }
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

  private boolean isValidKeywordsFormat(String response) {
    if (response == null || response.trim().isEmpty()) return false;
    String trimmed = response.trim();
    int commaCount = countCharacters(trimmed, ',');
    if (commaCount < 2) return false;
    if (trimmed.matches(".*\\d+\\..*")) return false;
    if (trimmed.startsWith(":") || trimmed.startsWith("-") || trimmed.startsWith("•")) return false;
    String[] keywords = trimmed.split(",");
    if (keywords.length != 3) return false;
    for (String keyword : keywords) {
      if (keyword.trim().isEmpty()) return false;
    }
    return true;
  }

  private int countCharacters(String str, char ch) {
    int count = 0;
    for (char c : str.toCharArray()) {
      if (c == ch) count++;
    }
    return count;
  }

  private List<String> getKeySkillsForVacancy(JSONArray keySkillsArray) {
    List<String> skills = new ArrayList<>();
    if (keySkillsArray == null) return skills;
    for (int i = 0; i < keySkillsArray.length(); i++) {
      JSONObject skill = keySkillsArray.getJSONObject(i);
      skills.add(skill.getString("name"));
    }
    return skills;
  }

  private String parseSalary(JSONObject salaryObj) {
    if (salaryObj == null) return null;
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
      // ignore
    }
    return null;
  }

  private String sendGetRequest(String urlString) throws Exception {
    URL url = new URL(urlString);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setRequestProperty("User-Agent", "HH-Parser/1.0");
    connection.setRequestProperty("Accept", "application/json");
    connection.setRequestProperty("Accept-Charset", "UTF-8");

    int responseCode = connection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
      StringBuilder response = new StringBuilder();
      String inputLine;
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