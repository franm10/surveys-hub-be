package sdcc.surveyshub.model.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sdcc.surveyshub.model.entity.Question;
import sdcc.surveyshub.model.entity.QuestionAnswer;
import sdcc.surveyshub.model.entity.SubmissionSurvey;
import sdcc.surveyshub.model.record.User;
import sdcc.surveyshub.model.record.request.SubmissionSurveyRequest;
import sdcc.surveyshub.model.record.response.QuestionResponse;
import sdcc.surveyshub.model.record.response.SubmissionSurveyCompleteResponse;
import sdcc.surveyshub.model.record.response.SubmissionSurveyResponse;
import sdcc.surveyshub.utils.DateUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper(componentModel = "spring", imports = { Instant.class, DateUtils.class })
public interface SubmissionSurveyMapper {

    @Mapping(target="id", source="user.uid")
    @Mapping(target="submittedBy", source="user.name")
    @Mapping(target="submittedByEmail", source="user.email")
    @Mapping(target="submittedAt", expression="java(DateUtils.now())")
    @Mapping(target="surveyId", source="req.surveyId")
    @Mapping(target="answers", expression="java(mapToList(req.answers()))")
    SubmissionSurvey toEntity(SubmissionSurveyRequest req, User user);

    SubmissionSurveyResponse toResponse(SubmissionSurvey submissionSurvey);

    @Mapping(target = "answers", expression = "java(mapAnswers(submission.getAnswers(), allQuestions, questionMapper))")
    SubmissionSurveyCompleteResponse toCompleteResponse(SubmissionSurvey submission, List<Question> allQuestions, @Context QuestionMapper questionMapper);

    default List<QuestionAnswer> mapToList(Map<Integer, Set<Integer>> answersMap) {
        if( answersMap == null || answersMap.isEmpty() )
            return List.of();

        return answersMap.entrySet()
                .stream()
                .map(e -> new QuestionAnswer(e.getKey(), e.getValue().stream().toList()))
                .toList();
    }

    default List<QuestionResponse> mapAnswers(List<QuestionAnswer> submitted, List<Question> allQuestions, QuestionMapper questionMapper) {
        return submitted.stream()
                .flatMap(sa -> allQuestions.stream()
                        .filter(q -> q.getNumSequence() == sa.getQuestionSeq())
                        .map(orig -> {
                            List<Integer> sel = sa.getAnswersSeq();

                            // copia shallow per non toccare l’originale
                            Question copy = questionMapper.copy(orig);
                            copy.setQuestionChoices(
                                    orig.getQuestionChoices().stream()
                                            .filter(c -> sel.contains(c.getNumSequence()))
                                            .toList());

                            return questionMapper.toResponse(copy);
                        }))
                .toList();
    }

}