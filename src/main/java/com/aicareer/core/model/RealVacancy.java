package com.aicareer.core.model;

public class RealVacancy {
  private String nameOfVacancy;
  private String salary;
  private String vacancyRequirements;

  public RealVacancy(String nameOfVacancy, String vacancyRequirements, String salary){
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
