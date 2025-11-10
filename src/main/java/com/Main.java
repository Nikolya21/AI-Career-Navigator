package com;

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

import java.util.List;
import java.util.logging.Logger;

public class Main {

  // Используем стандартный Java-логгер (или замените на SLF4J/Lombok @Slf4j)
  private static final Logger log = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
    
  }
}