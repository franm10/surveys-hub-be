package sdcc.surveyshub.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sdcc.surveyshub.model.entity.Question;
import sdcc.surveyshub.model.entity.Stats;
import sdcc.surveyshub.model.record.response.QuestionChoiceStatsResponse;
import sdcc.surveyshub.model.record.response.QuestionStatsResponse;
import sdcc.surveyshub.model.record.response.StatsResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface StatsMapper {

    @Mapping(target = "id", source = "stats.id")
    @Mapping(target = "totalSubmitted", source = "stats.totalSubmitted")
    @Mapping(target = "firstSubmitted", source = "stats.firstSubmitted")
    @Mapping(target = "lastSubmitted", source = "stats.lastSubmitted")
    @Mapping(target = "questionsStats", expression = "java(mapStats(stats.getQuestionStats(), questions))")
    StatsResponse toResponse(Stats stats, List<Question> questions);

    default List<QuestionStatsResponse> mapStats(Map<String, Map<String, Long>> questionStats, List<Question> questions) {
        return questions.stream()
                .map(q -> {
                    String qKey = String.valueOf(q.getNumSequence());
                    Map<String, Long> counts =
                            questionStats.getOrDefault(qKey, Collections.emptyMap());

                    List<QuestionChoiceStatsResponse> choiceResponses =
                            q.getQuestionChoices().stream()
                                    .map(qc -> {
                                        String cKey = String.valueOf(qc.getNumSequence());
                                        long count = counts.getOrDefault(cKey, 0L);
                                        return new QuestionChoiceStatsResponse(
                                                qc.getNumSequence(),
                                                qc.getText(),
                                                qc.getImageUrl(),
                                                count
                                        );
                                    })
                                    .collect(Collectors.toList());

                    return new QuestionStatsResponse(
                            q.getNumSequence(),
                            q.getText(),
                            q.isAllowMultipleAnswers(),
                            q.getImageUrl(),
                            choiceResponses
                    );
                })
                .collect(Collectors.toList());
    }

}