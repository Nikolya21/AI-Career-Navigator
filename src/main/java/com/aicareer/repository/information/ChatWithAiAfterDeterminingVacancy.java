package com.aicareer.repository.information;

import com.aicareer.core.model.CourseRequirements;

import java.util.List;

public interface ChatWithAiAfterDeterminingVacancy {

  List<String> generatePersonalizedQuestions(FinalVacancyRequarements requarements);

  void askingPersonalizedQuestions(List<String> generatedPersonalizedQuestions);

  String askingQuestion(String question);

  String continueDialogWithUser(String userAnswer);



  CourseRequirements analyzeCombinedData(FinalVacancyRequarements requarements);
}