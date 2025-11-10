package com.aicareer.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PotentialVacancy {
  private String nameOfVacancy;
  private String salary;
  private String infAboutVacancy;

  public String getVacancyRequirements() {
    return infAboutVacancy;
  }
  @Override
  public String toString(){
    return String.format("Должность: %s\nЗарплата: %s\nИнформация по вакансии: %s\n",
        nameOfVacancy, salary != null ? salary : "не указана", infAboutVacancy
    );
  }
}
