package com.aicareer.core.service.parserOfVacancy;

import com.aicareer.core.model.vacancy.FinalVacancyRequirements;
import com.aicareer.core.model.vacancy.PotentialVacancy;
import com.aicareer.core.model.vacancy.RealVacancy;
import com.aicareer.core.model.vacancy.SelectedPotentialVacancy;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.repository.parsing.SelectOfVacancy;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import com.aicareer.core.service.gigachat.GigaChatService;
import java.util.Scanner;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class SelectVacancy implements SelectOfVacancy {
  Scanner scan = new Scanner(System.in);
  private List<String> listOfThreeVacancy = new ArrayList<>();
  private SelectedPotentialVacancy selectedVacancy;
  private List<RealVacancy> parsedVacancies = new ArrayList<>();
  private String analysisResult;
  private final GigaChatService gigaChatService;


  @Override
  public String analyzeUserPreference(UserPreferences infoAboutPerson) {
    String promtAnalyze = (infoAboutPerson.toString() + "%s\n\nРоль: Ты — опытный HR-аналитик и карьерный психолог. Твоя задача — проанализировать диалог с пользователем, составить его детальный психологический портрет и на его основе подобрать три наиболее подходящие профессии.\n"
        + "\nКонтекст:\n"
        + "Пользователь прошел сессию карьерного консультирования. Тебе нужно глубоко проанализировать его ответы, чтобы понять его истинные мотивы, предпочтения и на основе этого предложить конкретные вакансии.\n"
        + "\nЗадача: Проведи комплексный анализ, а затем подбери профессии.\n"
        + "\nФормат вывода:\n"
        + "ПСИХОЛОГИЧЕСКИЙ ПОРТРЕТ ДЛЯ ПОДБОРА ВАКАНСИЙ\n"
        + "[анализ...]\n"
        + "ПОДБОР ПРОФЕССИЙ\n"
        + "[профессии...]\n"
        + ":::Профессия1,Профессия2,Профессия3\n"
        + "последняя строчка является обязательной и самым главным условием, так же учитывай что названия профессия должны быть на английском языке обязательно\n"
        + "ПРИМЕР: Javamiddle - \n"
        + "QA tester - \n"
        + "TeamLead -  \n"
        + "::: Javamiddle,Qa tester,TeamLead \n"
        + "после этих трех профессий ты должен закончить ответ и не добавлять больше символов!!!");
    String gigachatAnswer = gigaChatService.sendMessage(promtAnalyze);
    this.analysisResult = gigachatAnswer;
    extractThreeVacancies(gigachatAnswer);
    return gigachatAnswer;
  }

  @Override
  public List<String> extractThreeVacancies(String gigachatAnswer) {
    System.out.println(gigachatAnswer);
    if (gigachatAnswer.contains(":::")) { //todo тут беда с форматом - упало на тесте, когда gigachat вывел:
                                          //::
                                          //Data Scientist, Business Analyst, Analytics Manager
      String[] parts = gigachatAnswer.split(":::");
      System.out.println("PastsArray.toString() " + Arrays.toString(parts));
      if (parts.length < 3) {
        gigachatAnswer = validateAndFixResponse(gigachatAnswer);
        extractThreeVacancies(gigachatAnswer);
      }
      System.out.println(Arrays.toString(parts));
      if (parts.length > 1) {
        String vacanciesPart = parts[1].trim();
        String[] vacanciesArray = vacanciesPart.split(",");

        listOfThreeVacancy.clear();
        for (String vacancy : vacanciesArray) {
          listOfThreeVacancy.add(vacancy.trim());
        }

        while (listOfThreeVacancy.size() < 3) {
          listOfThreeVacancy.add("Вакансия " + (listOfThreeVacancy.size() + 1));
        }
      } else { //todo else (нужен нормальный рерол, если результата нет)
        String validateString = validateAndFixResponse(gigachatAnswer);
        extractThreeVacancies(validateString);
      }
    }
    System.out.println("Предложенные вакансии: " + listOfThreeVacancy);
   return listOfThreeVacancy;
  }

  public String validateAndFixResponse(String rawResponse) {
    // Сначала проверим простые случаи которые можно починить без запроса к нейронке
    System.out.println("validateAndFixResponse ACTIVATE");
    String simplified = preprocessResponse(rawResponse);
    if (isValidFormat(simplified)) {
      return simplified;
    }

    // Если простые методы не помогли - используем нейронку для исправления
    String validationPrompt = """
        КРИТИЧЕСКАЯ ЗАДАЧА: Исправь формат данных строго по шаблону
        
        ТВОЯ РОЛЬ: Форматировщик данных
        ЦЕЛЬ: Привести данные к строгому шаблону
        
        ШАБЛОН (обязателен):
        ":::Профессия1,Профессия2,Профессия3"
        
        ПРАВИЛА:
        - Начало: ровно 3 двоеточия ":::"
        - Затем 3 профессии через запятую
        - Без переносов строк
        - Без лишних символов
        - Только латиница/кириллица, запятые и пробелы между словами
        
        ДАННЫЕ ДЛЯ ИСПРАВЛЕНИЯ:
        %s
        
        КРИТИЧЕСКИЕ ТРЕБОВАНИЯ:
        1. Если профессий > 3 - оставь первые 3
        2. Если профессий < 3 - дополни до 3 (используй логику контекста)
        3. Удали все лишние символы, кроме букв, запятых и пробелов в названиях
        4. Убери все переносы строк
        5. Убедись в начале ровно ":::"
        
        ВЕРНИ ТОЛЬКО ИСПРАВЛЕННУЮ СТРОКУ БЕЗ ОБЪЯСНЕНИЙ!
        Пример: ":::Data Scientist,Business Analyst,Analytics Manager"
        """;

    return gigaChatService.sendMessage(String.format(validationPrompt, rawResponse));
  }

  private String preprocessResponse(String raw) {
    if (raw == null) return ":::Программист,Аналитик,Менеджер";

    // Убираем переносы строк и лишние пробелы
    String cleaned = raw.replace("\n", "").replace("\r", "").trim();

    // Простая попытка почистить формат
    if (cleaned.startsWith("::") && !cleaned.startsWith(":::")) {
      cleaned = ":::" + cleaned.substring(2);
    } else if (cleaned.startsWith(":") && !cleaned.startsWith(":::")) {
      cleaned = ":::" + cleaned.substring(1);
    } else if (!cleaned.startsWith(":::")) {
      cleaned = ":::" + cleaned;
    }

    return cleaned;
  }

  private boolean isValidFormat(String response) {
    return response != null &&
            response.startsWith(":::") &&
            response.length() > 3 &&
            countCommas(response) >= 2; // Должно быть минимум 2 запятые для 3 профессий
  }

  private int countCommas(String str) {
    int count = 0;
    for (char c : str.toCharArray()) {
      if (c == ',') count++;
    }
    return count;
  }


  @Override
  public SelectedPotentialVacancy choosenVacansy(List<String> listOfThreeVacancy) {
      System.out.println("Выберите одну из трех предложенных вакансий:");
      for (int i = 0; i < listOfThreeVacancy.size(); i++) {
        System.out.println((i + 1) + ". " + listOfThreeVacancy.get(i));
      }

      int chosenNumber = scan.nextInt();
      scan.nextLine();

      if (chosenNumber > 0 && chosenNumber <= listOfThreeVacancy.size()) {
        String chosenVacancyName = listOfThreeVacancy.get(chosenNumber - 1);

        PotentialVacancy potentialVacancy = new PotentialVacancy();
        potentialVacancy.setNameOfVacancy(chosenVacancyName);

        this.selectedVacancy = new SelectedPotentialVacancy(
            potentialVacancy
        );
      }


    System.out.println("Выбрана вакансия: " +
        (selectedVacancy != null ? selectedVacancy.getNameOfVacancy() : "не выбрана"));

    return selectedVacancy;
  }

  @Override
  public String formingByParsing(SelectedPotentialVacancy selectedVacancy) {

    String selectedVacancy1 = this.selectedVacancy.getNameOfVacancy();
    List<RealVacancy> vacancies = ParserService.getVacancies(selectedVacancy1, "1", 100);
    String newPromt = "";
    int neededCountOfVacancies = 0;
    for (int i = 0; i < Math.min(100, vacancies.size()); i++) {
      if (vacancies.get(i).getVacancyRequirements() != null || vacancies.get(i).getSalary() != null) {

        RealVacancy vacancy = vacancies.get(i);
        if (vacancy.getVacancyRequirements() != null) {
          newPromt += (vacancy.getNameOfVacancy() + "\n" + vacancy.getSalary() + "\n");
          for (int j = 0; j < vacancy.getVacancyRequirements().size(); j++) {
            newPromt += vacancy.getVacancyRequirements().get(j);
          }
          neededCountOfVacancies++;
        } else{
          newPromt += (vacancy.getNameOfVacancy() + "\n" + vacancy.getSalary() + "\n");
        }
      }
      if (neededCountOfVacancies == 3){
        break;
      }
    }



    return newPromt;
  }

  @Override
  public FinalVacancyRequirements formingFinalVacancyRequirements(String newPromt) {
      String gigachatFinalReqirements = gigaChatService.sendMessage(newPromt);
    return new FinalVacancyRequirements(gigachatFinalReqirements);
  }
}
