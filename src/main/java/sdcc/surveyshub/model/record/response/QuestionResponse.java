package sdcc.surveyshub.model.record.response;

import sdcc.surveyshub.model.entity.QuestionChoice;

import java.util.List;

public record QuestionResponse(

        String surveyId,
        Integer numSequence,

        String text,
        boolean allowMultipleAnswers,
        List<QuestionChoice> questionChoices,

        String imageUrl

) { }
