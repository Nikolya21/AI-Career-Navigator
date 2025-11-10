package com.aicareer.module.parsing;

import com.aicareer.core.model.FinalVacancyRequirements;
import com.aicareer.core.model.RealVacancy;
import com.aicareer.core.model.SelectedPotentialVacancy;
import com.aicareer.core.model.UserPreferences;
import java.util.List;

public interface SelectOfVacancy {
  String analyzeUserPreference(UserPreferences infoAboutPerson);

  SelectedPotentialVacancy choosenVacansy(List<String> listPotentialVacancy);

  String FormingByParsing(SelectedPotentialVacancy selectedVacancy);

  String FormingFinalVacancyRequirements(String newPromt);

}