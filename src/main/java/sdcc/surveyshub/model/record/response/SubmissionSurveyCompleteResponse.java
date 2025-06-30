package sdcc.surveyshub.model.record.response;

import java.time.Instant;
import java.util.List;

public record SubmissionSurveyCompleteResponse (

        String submittedBy,
        String submittedByEmail,
        Instant submittedAt,

        String surveyId,

        List<QuestionResponse> answers

) { }
