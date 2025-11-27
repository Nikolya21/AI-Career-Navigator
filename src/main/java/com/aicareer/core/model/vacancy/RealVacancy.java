package com.aicareer.core.model.vacancy;

import java.util.List;

public class RealVacancy {
  private String nameOfVacancy;
  private String salary;
  private String experience;
  private List<String> vacancyRequirements;

  public RealVacancy(String nameOfVacancy, List<String> vacancyRequirements, String salary, String experience){
    this.nameOfVacancy = nameOfVacancy;
    this.vacancyRequirements = vacancyRequirements;
    this.salary = salary;
    this.experience = experience;
  }

  public String getNameOfVacancy() {
    return nameOfVacancy;
  }

  public String getSalary() {
    return salary;
  }

  public String getExperience() {
    return experience;
  }

  public List<String> getVacancyRequirements() {
    return vacancyRequirements;
  }
  @Override
  public String toString(){
    return String.format("Зарплата: %s\nОпыт: %s\n",
        nameOfVacancy, salary != null ? salary : "не указана", vacancyRequirements
        );
  }
}
