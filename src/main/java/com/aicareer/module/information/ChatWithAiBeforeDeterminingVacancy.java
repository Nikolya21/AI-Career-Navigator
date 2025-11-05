package com.aicareer.module.information;

import com.aicareer.core.model.CVData;
import com.aicareer.core.model.UserPreferences;

import java.util.List;

public interface ChatWithAiBeforeDeterminingVacancy {

    String starDialogWithUser();

    void askingStandardQuestions();

    String askingStandardQuestion(String question);

    String continueDialogWithUser(String userAnswer);

    List<String> generatePersonalizedQuestions(CVData cvData);

    void askingPersonalizedQuestions(List<String> generatedPersonalizedQuestions);

    String askingPersonalizedQuestion(String question);

    UserPreferences analyzeCombinedData(String dialogHistory);

}
