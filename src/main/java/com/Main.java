package com;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import com.aicareer.core.model.RealVacancy;
import com.aicareer.core.model.SelectedPotentialVacancy;
import com.aicareer.core.service.ParserOfVacancy.ParserService;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    List<RealVacancy> vacancies = ParserService.getVacancies("Java middle developer", "1", 10);
    String newPromt = "";
    for (int i = 0; i < Math.min(10, vacancies.size()); i++) {
      RealVacancy vacancy = vacancies.get(i);
      newPromt += (vacancy + "\n");

    }

    System.out.println(newPromt);
    }
  }
