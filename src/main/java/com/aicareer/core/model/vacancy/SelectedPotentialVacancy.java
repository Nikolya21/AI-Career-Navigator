package com.aicareer.core.model.vacancy;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SelectedPotentialVacancy {
  private String nameOfVacancy;
  public SelectedPotentialVacancy(PotentialVacancy nameOfVacancy){
    this.nameOfVacancy = String.valueOf(nameOfVacancy);
  }

  public String getNameOfVacancy() {
    return nameOfVacancy;
  }
}
