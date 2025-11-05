package com.aicareer.core.model;

import java.util.List;

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
  @Override
  public String toString(){
    return String.format("Должность: %s\nЗарплата: %s\nОпыт: %s\n",
        nameOfVacancy, salary != null ? salary : "не указана", vacancyRequirements
        );
  }
}
