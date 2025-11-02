package com.aicareer.core.service.roadmap;

import com.aicareer.core.model.Roadmap;
import com.aicareer.core.model.RoadmapZone;
import com.aicareer.core.model.Week;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.module.roadmap.RoadmapGenerate;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Data
public class RoadmapGenerateService implements RoadmapGenerate {

    private final GigaChatService gigaChatApiService;
    @Override
    public String gettingWeeksInformation(List<Week> weeks) {
        StringJoiner builder = new StringJoiner("\n");

        for (Week week : weeks) {
            String infoAboutWeek = week.getTastForTheWeek();
            builder.add(infoAboutWeek);
        }
        return builder.toString();
    }

    @Override
    public String informationComplexityAndQuantityAnalyzeAndCreatingZone(String weeksInformation) {

        String prompt = String.format("Роль: Ты — опытный методист и специалист по педагогическому дизайну. Твоя задача — проанализировать структуру курса и разбить его на логические тематические зоны для оптимального восприятия студентами.\n" +
                "    \n" +
                "    Контекст:\n" +
                "    Ты получаешь детальный план курса, разбитый по неделям. Каждая неделя содержит список учебных задач и материалов. Необходимо сгруппировать недели в тематические зоны, сохраняя последовательность и учитывая ментальную нагрузку на студентов.\n" +
                "    \n" +
                "    Данные курса для анализа:\n" +
                "    %s\n" +
                "    \n" +
                "    Задача: Проведи комплексный анализ и распредели недели по зонам:\n" +
                "    \n" +
                "    1. Анализ содержания каждой недели:\n" +
                "       - Определи основную тему и подтемы каждой недели\n" +
                "       - Оцени сложность задач (базовые/продвинутые/практические)\n" +
                "       - Проанализируй объем информации и ментальную нагрузку\n" +
                "       - Выяви связи между неделями и последовательность тем\n" +
                "    \n" +
                "    2. Анализ тематических переходов:\n" +
                "       - Найди естественные границы между темами\n" +
                "       - Определи точки, где происходит смена контекста или сложности\n" +
                "       - Выяви недели-мосты, которые связывают разные темы\n" +
                "       - Отметь ключевые вехи в освоении курса\n" +
                "    \n" +
                "    3. Формирование тематических зон:\n" +
                "       - Сгруппируй недели в логические блоки (зоны) по 2-4 недели в каждой\n" +
                "       - Сохрани хронологический порядок недель внутри зон\n" +
                "       - Учти когнитивную нагрузку - не перегружай зоны\n" +
                "       - Создай плавные переходы между зонами\n" +
                "    \n" +
                "    4. Критерии формирования зоны:\n" +
                "       - Одна зона = одна завершенная тема или навык\n" +
                "       - Баланс теории и практики внутри зоны\n" +
                "       - Постепенное нарастание сложности\n" +
                "       - Возможность увидеть результат после каждой зоны\n" +
                "    \n" +
                "    Требования к результату:\n" +
                "       - Зоны должны быть мотивирующими (студент видит прогресс)\n" +
                "       - Каждая зона должна иметь четкую учебную цель\n" +
                "       - Сохранить логическую целостность курса\n" +
                "       - Учесть психологию восприятия (не более 4 недель в зоне)\n" +
                "    \n" +
                "    Формат вывода: Каждая зона в отдельной строке в формате:\n" +
                "    \"Название зоны\" + \" | \" + \"[номер недели, с которой начинается зона, и номер недели на которой заканчивается зона (вкл)]\" + \" | \" + \"Чему пользователь научится после этой зоны\" + \" | \" + \"начальный/средний/продвинутый\" + \" * \"\n" +
                "    \n" +
                "    Пример:\n" +
                "    \"Основы программирования\" | 1 3 | \"Сможет писать простые программы на Python, понимать базовые структуры данных\" | начальный *\n" +
                "    \"Объектно-ориентированное программирование\" | 4 6 | \"Научится создавать классы и объекты, понимать принципы ООП\" | средний *\n" +
                "    \n" +
                "    Важные принципы:\n" +
                "    - Не разбивай связанные темы по разным зонам\n" +
                "    - Учитывай постепенное усложнение материала\n" +
                "    - Создавай ощущение достижения после каждой зоны\n" +
                "    - Сохраняй педагогическую логику курса\n" +
                "    - В конце каждой зоны ставь \" * \" как разделитель" + "\n" +
                "    Данные курса для анализа:\n%s", weeksInformation
        );

        return gigaChatApiService.sendMessage(prompt);
    }

    @Override
    public List<RoadmapZone> splittingWeeksIntoZones(String resultOfComplexityAndQuantityAnalyze, List<Week> weeks) {

        List<String> roadmapZoneInString = List.of(resultOfComplexityAndQuantityAnalyze.split("\\*"));

        List<RoadmapZone> roadmapZones = new ArrayList<>();
        for (String zone : roadmapZoneInString) {
            List<String> zoneFieldsInString = List.of(zone.split("\\|"));

            RoadmapZone roadmapZone = new RoadmapZone();

            roadmapZone.setName(zoneFieldsInString.get(0).trim());
            roadmapZone.setLearningGoal(zoneFieldsInString.get(2).trim());
            roadmapZone.setComplexityLevel(zoneFieldsInString.get(3).trim());

            List<String> helperForRangeWeeks = List.of(zoneFieldsInString.get(1).trim().split(" "));

            int start = Integer.parseInt(helperForRangeWeeks.get(0));
            int end = Integer.parseInt(helperForRangeWeeks.get(1));

            roadmapZone.setWeeks(weeks.subList(start, end + 1));

            roadmapZones.add(roadmapZone);
        }
        return roadmapZones;
    }

    @Override
    public Roadmap identifyingThematicallySimilarZones(List<RoadmapZone> roadmapZones) {
        Roadmap roadmap = new Roadmap();
        for (RoadmapZone roadmapZone : roadmapZones) {
            roadmap.addRoadmapZone(roadmapZone);
        }
        return roadmap;
    }
}
