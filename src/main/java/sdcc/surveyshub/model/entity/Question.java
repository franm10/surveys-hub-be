package sdcc.surveyshub.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Question {

    private String surveyId;

    private String id;
    private int numSequence;

    private String text;
    private boolean allowMultipleAnswers;
    private List<QuestionChoice> questionChoices;

    private String imageUrl;

}