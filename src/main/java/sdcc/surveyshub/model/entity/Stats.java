package sdcc.surveyshub.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
public class Stats {

    private String id;      // surveyId --> non serve in realta

    private Long totalSubmitted;

    private Instant firstSubmitted;

    private Instant lastSubmitted;

    private Map<String, Map<String, Long>> questionStats;        //Map <questionSeq, Map <choiceSeq, count> >

}
