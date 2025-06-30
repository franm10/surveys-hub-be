package sdcc.surveyshub.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class SubmissionSurvey {

    private String id;                  //user.uid
    private String submittedBy;         //user.name
    private String submittedByEmail;    //user.email
    private Instant submittedAt;

    private String surveyId;

    private List<QuestionAnswer> answers;     // Map QuestionSeq.toString : Set<QuestionChoiceSeq>

}
