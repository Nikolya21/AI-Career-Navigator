package com.aicareer;

import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.parserOfVacancy.ParserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Main {

  public static void main(String[] args) {
    GigaChatService gigaChatService = new GigaChatService();
    ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);

    // Получаем бин ParserService
    ParserService parserService = context.getBean(ParserService.class);

    // Вызываем метод
    var vacancies = parserService.getVacancies("Python", "1", 2);
    vacancies.forEach(v -> {
      if (!v.getVacancyRequirements().isEmpty()) {
        System.out.println(v.getNameOfVacancy() + "   " + v.getVacancyRequirements());
      } else {
        String answer = gigaChatService.sendMessage(
            "Ниже тебе будет дано описании вакансии, найди и вычлени только ключевые навыки связанные с айти и программированием напрямую, которые требуют : \n"
                + v.getDescription()
                + "в ответ дай только 5-10 основных навыков в формате [Python, SQL, Linux, OpenOffice, Java Spring] обязательно обернув в квадрантые скобочки и не добавляя кавычек, а также в одну строку");

        System.out.println(v.getNameOfVacancy() + " " + answer);
      }
    });

    context.close();
  }
}