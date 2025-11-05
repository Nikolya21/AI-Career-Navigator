package com.aicareer.module.information;

import com.aicareer.core.model.CourseRequirements;
import com.aicareer.core.model.FinalVacancyRequirements;

import java.util.List;

public interface ChatWithAiAfterDeterminingVacancy {

    List<String> generatePersonalizedQuestions(FinalVacancyRequirements requarements);

    void askingPersonalizedQuestions(List<String> generatedPersonalizedQuestions);

    String askingQuestion(String question);

    String continueDialogWithUser(String userAnswer);

    CourseRequirements analyzeCombinedData(FinalVacancyRequirements requarements);
}