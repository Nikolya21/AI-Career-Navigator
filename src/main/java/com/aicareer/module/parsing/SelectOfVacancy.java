package com.aicareer.module.parsing;

import java.util.List;

public interface SelectOfVacancy {
  String analyzeUserPreference(UserPreferences infoAboutPerson);

  List<SelectedPotentialVacancy> createPotentialVacancy(String resultOfAnalyzeUserPreference);

  SelectedPotentialVacancy chosenVacancy(List<PotentialVacancy> listPotentialVacancy);

  List<RealVacancy> FormingByParsing(SelectedPotentialVacancy selectedVacancy);

  FinalVacancyRequirements FormingFinalVacancyRequirements(List<RealVacancy> RealParsedVacancy);

}