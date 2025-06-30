package sdcc.surveyshub.model.record.response;

import java.util.List;

public record QuestionStatsResponse(
        int numSequence,
        String text,
        boolean allowMultipleAnswers,
        String imageUrl,
        List<QuestionChoiceStatsResponse> choices
) { }
