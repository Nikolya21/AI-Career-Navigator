package com.aicareer.core.service.information.prompts;

public final class AdaptationPrompts {

  private AdaptationPrompts() {
    throw new AssertionError("Cannot instantiate utility class");
  }

  public static final String ANALYZE_PREFERENCES = """
            На основе приведённого ниже диалога с пользователем, который планирует войти в IT,
            составь его подробный профиль. Включи следующие аспекты:
            
            1. Доступное время на обучение в неделю (weeklyHours) — укажи конкретное число или диапазон.
            2. Предпочитаемый формат материалов (preferredFormat) — видео, статьи, интерактив, аудио, смешанный и т.д.
            3. Основной драйвер и цели (motivation) — финансы, интерес, удалёнка, смена профессии, самореализация и пр.
            4. Имеющийся бэкграунд (background) — смежные навыки, хобби, образование, опыт работы, даже не связанный с IT.
            5. Предполагаемый уровень мотивации (motivationLevel) — низкий, средний, высокий.
            
            ВАЖНО: Верни результат строго в формате JSON без каких-либо пояснений, комментариев или дополнительного текста.
            Используй только указанные ниже поля:
            
            {
              "weeklyHours": "строка",
              "preferredFormat": "строка",
              "motivation": "строка",
              "background": "строка",
              "motivationLevel": "низкий/средний/высокий"
            }
            
            Диалог:
            %s
            """;
}