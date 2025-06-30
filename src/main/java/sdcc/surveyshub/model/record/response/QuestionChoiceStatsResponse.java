package sdcc.surveyshub.model.record.response;

public record QuestionChoiceStatsResponse(
        int numSequence,
        String text,
        String imageUrl,
        long count
) { }
