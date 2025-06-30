package sdcc.surveyshub.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sdcc.surveyshub.model.entity.Question;
import sdcc.surveyshub.model.entity.QuestionChoice;
import sdcc.surveyshub.model.record.request.QuestionRequest;
import sdcc.surveyshub.model.record.response.QuestionResponse;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = { UUID.class, List.class })
public interface QuestionMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "surveyId", expression = "java(surveyId)")
    @Mapping(target = "numSequence", expression = "java(numSequence)")
    @Mapping(target = "text", source = "req.text")
    @Mapping(target = "allowMultipleAnswers", source = "req.allowMultipleAnswers")
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "questionChoices", ignore = true)
    Question toBaseEntity(QuestionRequest req, String surveyId, int numSequence);

    QuestionResponse toResponse(Question question);

    @Mapping(target = "questionChoices", source = "questionChoices")
    Question copy(Question original);

    /** Used by MapStruct for copy List<QuestionChoices>, must have same name of Question.copy for working*/
    @SuppressWarnings("unused")
    QuestionChoice copy(QuestionChoice original);

}