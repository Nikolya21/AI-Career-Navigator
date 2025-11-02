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
  public static List<RealVacancy> getVacancies(String searchText, String area, int perPage){
    List<RealVacancy> vacancies = new ArrayList<>();
    try {
      String apiUrl = String.format(
          "https://api.hh.ru/vacancies?text=%s&area=%s&per_page=%d",
          searchText.replace(" ", "+"),
          area,
          perPage
      );
    }


  }
}
