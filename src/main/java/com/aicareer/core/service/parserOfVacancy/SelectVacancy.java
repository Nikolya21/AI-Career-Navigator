package com.aicareer.core.service.parserOfVacancy;

import com.aicareer.core.model.vacancy.FinalVacancyRequirements;
import com.aicareer.core.model.vacancy.PotentialVacancy;
import com.aicareer.core.model.vacancy.RealVacancy;
import com.aicareer.core.model.vacancy.SelectedPotentialVacancy;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.repository.parsing.SelectOfVacancy;
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
        + "::: Javamiddle,Qa tester,TeamLead \n");
    String gigachatAnswer = gigaChatService.sendMessage(promtAnalyze);
    this.analysisResult = gigachatAnswer;
    extractThreeVacancies(gigachatAnswer);
    return gigachatAnswer;
  }

  @Override
  public List<String> extractThreeVacancies(String gigachatAnswer) {
    System.out.println(gigachatAnswer);
    if (gigachatAnswer.contains(":::")) {
      String[] parts = gigachatAnswer.split(":::");
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
      }
    }
    System.out.println("Предложенные вакансии: " + listOfThreeVacancy);
   return listOfThreeVacancy;
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
      if (vacancies.get(i).getVacancyRequirements() != null || vacancies.get(i).getSalary() != null){
        neededCountOfVacancies++;
        RealVacancy vacancy = vacancies.get(i);
        newPromt += (vacancy.getNameOfVacancy() + "\n" +vacancy.getSalary() + "\n" +vacancy.getVacancyRequirements().get(0) + "\n");
      }
      if (neededCountOfVacancies == 10){
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
