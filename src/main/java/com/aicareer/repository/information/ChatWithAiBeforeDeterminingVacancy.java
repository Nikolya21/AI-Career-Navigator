
package com.aicareer.repository.information;

import com.aicareer.core.model.user.CVData;
import com.aicareer.core.model.userPreferences.UserPreferences;
import com.aicareer.core.model.user.CVData;

import java.util.List;

public interface ChatWithAiBeforeDeterminingVacancy {

    String starDialogWithUser();

    void askingStandardQuestions();

    String askingStandardQuestion(String question);

    String continueDialogWithUser(String userAnswer);

    List<String> generatePersonalizedQuestions(CVData cvData);


  void askingPersonalizedQuestions(List<String> generatedPersonalizedQuestions);

    String askingPersonalizedQuestion(String question);

    UserPreferences analyzeCombinedData();

}
