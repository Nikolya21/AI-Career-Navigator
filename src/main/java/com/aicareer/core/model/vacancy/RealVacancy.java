package com.aicareer.core.model.vacancy;

import java.util.List;
import lombok.Data;

@Data
public class RealVacancy {
  private String nameOfVacancy;
  private String salary;
  private List<String> vacancyRequirements;

  public RealVacancy(String nameOfVacancy, List<String> vacancyRequirements, String salary){
    this.nameOfVacancy = nameOfVacancy;
    this.vacancyRequirements = vacancyRequirements;
    this.salary = salary;
  }

  public String getNameOfVacancy() {
    return nameOfVacancy;
  }

  public String getSalary() {
    return salary;
  }

  public List<String> getVacancyRequirements() {
    return vacancyRequirements;
  }

}
