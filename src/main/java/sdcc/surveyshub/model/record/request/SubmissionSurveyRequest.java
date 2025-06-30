package sdcc.surveyshub.model.record.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.Set;

public record SubmissionSurveyRequest(

        @NotNull @NotEmpty String surveyId,

        @NotNull @NotEmpty Map<Integer, Set<Integer>> answers  // Map QuestionSeq : Set<QuestionChoiceSeq>

) { }
