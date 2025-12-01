package com.aicareer.core.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class URLValidator {

  public boolean isUrlValid(String urlString) {
    if (urlString == null || !urlString.startsWith("http")) {
      return false;
    }

    HttpURLConnection connection = null;
    BufferedReader reader = null;

    try {
      URL url = new URL(urlString);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);
      connection.setInstanceFollowRedirects(true);

      // Устанавливаем User-Agent чтобы избежать блокировок
      connection.setRequestProperty("User-Agent",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

      int responseCode = connection.getResponseCode();

      // 🔥 СТРОГАЯ ПРОВЕРКА: только 200-399 статусы
      if (responseCode < 200 || responseCode >= 400) {
        System.out.println("❌ URL недоступен: " + urlString + " (код: " + responseCode + ")");
        return false;
      }

      // 🔥 ПРОВЕРЯЕМ ЧТО СТРАНИЦА ИМЕЕТ КОНТЕНТ
      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String line;
      int contentLength = 0;
      while ((line = reader.readLine()) != null && contentLength < 1000) {
        contentLength += line.length();
      }

      if (contentLength < 100) {
        System.out.println("⚠️ URL имеет мало контента: " + urlString + " (" + contentLength + " chars)");
        return false;
      }

      System.out.println("✅ URL рабочий: " + urlString + " (" + contentLength + " chars)");
      return true;

    } catch (IOException e) {
      System.out.println("❌ Ошибка доступа к URL: " + urlString + " - " + e.getMessage());
      return false;
    } finally {
      if (reader != null) {
        try { reader.close(); } catch (IOException e) { }
      }
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  public List<String> filterValidUrls(List<String> urls) {
    List<String> validUrls = new ArrayList<>();
    System.out.println("🔍 Проверка " + urls.size() + " URL...");

    for (String url : urls) {
      if (isUrlValid(url)) {
        validUrls.add(url);
        // 🔥 ОГРАНИЧИВАЕМ 3 ССЫЛКИ НА ЗАДАЧУ
        if (validUrls.size() >= 3) {
          break;
        }
      }
    }

    return validUrls;
  }
}