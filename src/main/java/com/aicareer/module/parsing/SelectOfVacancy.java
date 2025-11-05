package com.aicareer.module.parsing;

import com.aicareer.core.model.FinalVacancyRequirements;
import com.aicareer.core.model.PotentialVacancy;
import com.aicareer.core.model.RealVacancy;
import com.aicareer.core.model.SelectedPotentialVacancy;
import com.aicareer.core.model.UserPreferences;
import java.util.List;

public interface SelectOfVacancy {
  String analyzeUserPreference(UserPreferences infoAboutPerson);

  SelectedPotentialVacancy chosenVacancy(List<PotentialVacancy> listPotentialVacancy);

  List<RealVacancy> FormingByParsing(SelectedPotentialVacancy selectedVacancy);

  FinalVacancyRequirements FormingFinalVacancyRequirements(List<RealVacancy> RealParsedVacancy);

}