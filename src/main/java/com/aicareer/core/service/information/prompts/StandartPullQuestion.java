package com.aicareer.core.service.information.prompts;

public final class StandartPullQuestion {

  private StandartPullQuestion() {
    throw new AssertionError("Cannot instantiate utility class");
  }

  public static final String TIME_QUESTION =
    "Сколько времени в неделю вы готовы выделить на обучение ?";

  public static final String INFORMATION_FORMAT =
    "Какой формат информации вы воспринимаете лучше всего ? - статьи / видео, может быть, что-то другое..." + "\n" +
      "По возможности распишите ваш опыт обучения с помощью интернет курсов (если он есть).";

  public static final String MOTIVATION_CONTEXT =
    "Почему вы решили войти в IT? Что для вас является главным драйвером?";

  public static final String BACKGROUND_QUESTION =
    "Есть ли у вас опыт в смежных сферах или хобби, связанных с логикой/творчеством?";

}
