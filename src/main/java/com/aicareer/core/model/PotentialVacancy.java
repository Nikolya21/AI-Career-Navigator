package com.aicareer.core.model;

public class PotentialVacancy {
  private String nameOfVacancy;
  private String salary;
  private String infAboutVacancy;
  public PotentialVacancy(String nameOfVacancy, String infAboutVacancy, String salary){
    this.nameOfVacancy = nameOfVacancy;
    this.infAboutVacancy = infAboutVacancy;
    this.salary = salary;
  }

  public String getNameOfVacancy() {
    return nameOfVacancy;
  }

  public String getSalary() {
    return salary;
  }

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
