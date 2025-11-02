package com.aicareer.core.service.ParserOfVacancy;

import com.aicareer.core.model.FinalVacancyRequirements;
import com.aicareer.core.model.PotentialVacancy;
import com.aicareer.core.model.RealVacancy;
import com.aicareer.core.model.SelectedPotentialVacancy;
import com.aicareer.core.model.UserPreferences;
import com.aicareer.module.parsing.SelectOfVacancy;
import java.util.List;

public class SelectVacancy implements SelectOfVacancy {

  @Override
  public String analyzeUserPreference(UserPreferences infoAboutPerson) {
    ;
  }

  @Override
  public List<SelectedPotentialVacancy> createPotentialVacancy(
      String resultOfAnalyzeUserPreference) {
    return List.of();
  }

  @Override
  public SelectedPotentialVacancy chosenVacancy(List<PotentialVacancy> listPotentialVacancy) {
    return null;
  }

  @Override
  public List<RealVacancy> FormingByParsing(SelectedPotentialVacancy selectedVacancy) {
    return List.of();
  }

  @Override
  public FinalVacancyRequirements FormingFinalVacancyRequirements(
      List<RealVacancy> RealParsedVacancy) {
    return null;
  }
}
