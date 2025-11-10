package com;

import com.aicareer.core.model.RealVacancy;
import com.aicareer.core.model.SelectedPotentialVacancy;
import com.aicareer.core.service.ParserOfVacancy.ParserService;
import java.util.List;
import com.aicareer.core.service.ParserOfVacancy.SelectVacancy;
import java.util.Scanner;

public class Main {

  public static void main(String[] args) {
  Scanner scanner = new Scanner(System.in);

  try{

    SelectVacancy selectVacancy = new SelectVacancy();

    System.out.println("\n🔍 Шаг 1: Анализ предпочтений пользователя...");
    String analysisResult = selectVacancy.analyzeUserPreference(userinfo);
    System.out.println("✅ Анализ завершен!");

    List<String> suggestedVacancies = selectVacancy.extractThreeVacancies(analysisResult);
    SelectedPotentialVacancy selectedVacancy = selectVacancy.choosenVacansy(suggestedVacancies);

    System.out.println("\n🌐 Шаг 3: Парсинг реальных вакансий с HH.ru...");
    String parsingResults = selectVacancy.FormingByParsing(selectedVacancy);
    System.out.println("✅ Парсинг завершен!");

    System.out.println("\n📝 Шаг 4: Формирование финальных требований...");
    String finalRequirements = selectVacancy.FormingFinalVacancyRequirements(parsingResults);
    System.out.println("✅ Финальные требования сформированы!");
    System.out.println(finalRequirements);
  } catch (Exception e){
    System.err.println("❌ Ошибка в работе SelectVacancy: " + e.getMessage());
    e.printStackTrace();
  }
  }
}