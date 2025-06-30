package sdcc.surveyshub.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnswer {

    private Integer questionSeq;

    private List<Integer> answersSeq;

}
