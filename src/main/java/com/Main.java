package com;

import com.aicareer.core.model.vacancy.SelectedPotentialVacancy;
import com.aicareer.core.service.ParserOfVacancy.ParserService;
import com.aicareer.core.DTO.courseDto.CourseRequest;
import com.aicareer.core.DTO.courseDto.ResponseByWeek;
import com.aicareer.core.Validator.LlmResponseValidator;
import com.aicareer.core.config.GigaChatConfig;
import com.aicareer.core.model.courseModel.CourseRequirements;
import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.vacancy.FinalVacancyRequirements;
import com.aicareer.core.service.course.*;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.information.ChatWithAiAfterDeterminingVacancyService;
import com.aicareer.core.service.information.ChatWithAiBeforeDeterminingVacancyService;
import com.aicareer.core.service.information.DialogService;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import com.aicareer.core.service.ParserOfVacancy.SelectVacancy;

import java.util.List;
import com.aicareer.core.service.ParserOfVacancy.SelectVacancy;
import java.util.Scanner;
import java.util.logging.Logger;

public class Main {


  // Используем стандартный Java-логгер (или замените на SLF4J/Lombok @Slf4j)
  private static final Logger log = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
  Scanner scanner = new Scanner(System.in);

  try{

    SelectVacancy selectVacancy = new SelectVacancy();

    System.out.println("\n🔍 Шаг 1: Анализ предпочтений пользователя...");
    String analysisResult = selectVacancy.analyzeUserPreference(userinfo);
    System.out.println("✅ Анализ завершен!");

    List<String> suggestedVacancies = selectVacancy.extractThreeVacancies(analysisResult);
    SelectedPotentialVacancy selectedVacancy = selectVacancy.choosenVacansy(suggestedVacancies);

    System.out.println("\n🌐 Шаг 3: Парсинг реальных вакансий с HH.ru...");
    String parsingResults = selectVacancy.FormingByParsing(selectedVacancy);
    System.out.println("✅ Парсинг завершен!");

    System.out.println("\n📝 Шаг 4: Формирование финальных требований...");
    String finalRequirements = selectVacancy.FormingFinalVacancyRequirements(parsingResults);
    System.out.println("✅ Финальные требования сформированы!");
    System.out.println(finalRequirements);
  } catch (Exception e){
    System.err.println("❌ Ошибка в работе SelectVacancy: " + e.getMessage());
    e.printStackTrace();
  }
  }
}

  }
}