package sdcc.surveyshub.model.record.response;

import sdcc.surveyshub.model.entity.QuestionAnswer;

import java.time.Instant;
import java.util.List;

public record SubmissionSurveyResponse (

        String submittedBy,
        String submittedByEmail,
        Instant submittedAt,

        String surveyId,

        List<QuestionAnswer> answers

) { }
