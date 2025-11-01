package com.aicareer.module.information;

import com.aicareer.core.model.UserPreferences;

public interface ChatWithAiBeforeDeterminingVacancy {

    String starDialogWithUser();

    String askingStandardQuestions();

    String continueDialogWithUser(String userAnswer);

    String generatePersonalizedQuestions(ResumeData resumeData);

    String askingPersonalizedQuestions(String generatedPersonalizedQuestions);

    UserPreferences analyzeCombinedData(String dialogHistory);

}
