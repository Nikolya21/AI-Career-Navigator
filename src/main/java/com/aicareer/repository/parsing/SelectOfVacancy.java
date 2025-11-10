package com.aicareer.repository.parsing;

import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.vacancy.SelectedPotentialVacancy;

import java.util.List;

public interface SelectOfVacancy {
  String analyzeUserPreference(UserPreferences infoAboutPerson);

  List<String> extractThreeVacancies(String gigachatAnswer);

  SelectedPotentialVacancy choosenVacansy(List<String> listPotentialVacancy);

  String FormingByParsing(SelectedPotentialVacancy selectedVacancy);

  String FormingFinalVacancyRequirements(String newPromt);

}