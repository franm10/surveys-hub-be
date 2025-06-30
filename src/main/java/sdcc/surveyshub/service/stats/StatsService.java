package sdcc.surveyshub.service.stats;

import sdcc.surveyshub.model.entity.Question;
import sdcc.surveyshub.model.entity.Stats;
import sdcc.surveyshub.model.entity.Survey;
import sdcc.surveyshub.model.record.User;
import sdcc.surveyshub.model.record.response.StatsResponse;

import java.util.List;

public interface StatsService {

    Stats createStatsSubCollections(Survey survey, List<Question> questions);

    StatsResponse getStatsFromSurvey(String surveyId, User user);

    StatsResponse getStatsFromPublicSurvey(String surveyId);

}
