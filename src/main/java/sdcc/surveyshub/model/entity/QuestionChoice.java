package sdcc.surveyshub.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionChoice {

    private int numSequence;

    private String text;

    private String imageUrl;

}
