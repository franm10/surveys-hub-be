package sdcc.surveyshub.model.record.response;

import java.time.Instant;
import java.util.List;

public record StatsResponse(
        String id,

        Long totalSubmitted,
        Instant firstSubmitted,
        Instant lastSubmitted,

        List<QuestionStatsResponse> questionsStats
) { }
