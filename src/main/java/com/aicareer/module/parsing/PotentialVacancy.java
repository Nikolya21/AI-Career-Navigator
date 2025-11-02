package com.aicareer.module.parsing;

public class PotentialVacancy {
  private String nameOfVacancy;
  private String salary;
  private String infAboutVacancy;
  int vacancyComplexity;
  public PotentialVacancy(String nameOfVacancy, String infAboutVacancy, String salary){
    this.nameOfVacancy = nameOfVacancy;
    this.infAboutVacancy = vacancyRequirements;
    this.salary = salary;
  }

  public String getNameOfVacancy() {
    return nameOfVacancy;
  }

  public String getSalary() {
    return salary;
  }

  public String getVacancyRequirements() {
    return vacancyRequirements;
  }
  @Override
  public String toString(){
    return String.format("Должность: %s\nЗарплата: %s\nОпыт: %s\n",
        nameOfVacancy, salary != null ? salary : "не указана", vacancyRequirements
    );
  }
}
