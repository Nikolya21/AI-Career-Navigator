package com.aicareer.core.service.ParserOfVacancy;

import com.aicareer.core.model.RealVacancy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class ParserService {

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

        List<String> keySkills = getKeySkillsForVacancy(id);

        vacancies.add(new RealVacancy(title,  keySkills, salary));
      }
    } catch (Exception e) {
      System.err.println("Ошибка при получении" + e.getMessage());
    }

    return vacancies;
  }
  private static List<String> getKeySkillsForVacancy(String vacancyId) {
    List<String> skills = new ArrayList<>();

    try {
      String vacancyUrl = "https://api.hh.ru/vacancies/" + vacancyId;
      String vacancyJson = sendGetRequest(vacancyUrl);

      JSONObject vacancyDetail = new JSONObject(vacancyJson);

      if (vacancyDetail.has("key_skills")) {
        JSONArray keySkillsArray = vacancyDetail.getJSONArray("key_skills");

        for (int i = 0; i < keySkillsArray.length(); i++) {
          JSONObject skill = keySkillsArray.getJSONObject(i);
          skills.add(skill.getString("name"));
        }
      }
    } catch (Exception e) {
      System.err.println("Ошибка при получении навыков для вакансии " + vacancyId + ": " + e.getMessage());
    }

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