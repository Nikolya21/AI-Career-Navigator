package com.aicareer.module.parsing;

import com.aicareer.core.model.vacancy.SelectedPotentialVacancy;

import java.util.List;

public interface SelectOfVacancy {
  String analyzeUserPreference(UserPreferences infoAboutPerson);

  SelectedPotentialVacancy choosenVacansy(List<String> listPotentialVacancy);

  String FormingByParsing(SelectedPotentialVacancy selectedVacancy);

  String FormingFinalVacancyRequirements(String newPromt);

}