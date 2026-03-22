package com.aicareer;

import com.aicareer.application.CareerNavigatorApplicationImpl;
import com.aicareer.core.config.DatabaseConfig;
import com.aicareer.core.service.course.LearningPlanAssembler;
import com.aicareer.core.service.course.ServiceGenerateCourse;
import com.aicareer.core.service.course.ServicePrompt;
import com.aicareer.core.service.course.ServiceWeek;
import com.aicareer.core.service.course.WeekDistributionService;
import com.aicareer.core.service.parserOfVacancy.SelectVacancy;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.information.ChatWithAiAfterDeterminingVacancyService;
import com.aicareer.core.service.information.ChatWithAiBeforeDeterminingVacancyService;
import com.aicareer.core.service.information.DialogService;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import com.aicareer.core.service.roadmap.RoadmapService;
import com.aicareer.core.service.user.UserService;
import com.aicareer.core.service.user.impl.UserServiceImpl;
import com.aicareer.presentation.ConsolePresentation;
import com.aicareer.repository.user.CVDataRepository;
import com.aicareer.repository.user.UserPreferencesRepository;
import com.aicareer.repository.user.UserRepository;
import com.aicareer.repository.user.UserSkillsRepository;
import com.aicareer.repository.user.impl.CVDataRepositoryImpl;
import com.aicareer.repository.user.impl.UserPreferencesRepositoryImpl;
import com.aicareer.repository.user.impl.UserRepositoryImpl;
import com.aicareer.repository.user.impl.UserSkillsRepositoryImpl;
import com.aicareer.repository.roadmap.RoadmapRepository;
import com.aicareer.repository.roadmap.RoadmapZoneRepository;
import com.aicareer.repository.roadmap.WeekRepository;
import com.aicareer.repository.roadmap.TaskRepository;
import com.aicareer.repository.roadmap.impl.RoadmapRepositoryImpl;
import com.aicareer.repository.roadmap.impl.RoadmapZoneRepositoryImpl;
import com.aicareer.repository.roadmap.impl.WeekRepositoryImpl;
import com.aicareer.repository.roadmap.impl.TaskRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class Main {
  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
}
