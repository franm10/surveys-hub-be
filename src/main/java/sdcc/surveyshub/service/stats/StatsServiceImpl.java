package sdcc.surveyshub.service.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sdcc.surveyshub.exception.NotFoundException;
import sdcc.surveyshub.model.dao.QuestionDao;
import sdcc.surveyshub.model.dao.StatsDao;
import sdcc.surveyshub.model.dao.SurveyDao;
import sdcc.surveyshub.model.entity.Question;
import sdcc.surveyshub.model.entity.QuestionChoice;
import sdcc.surveyshub.model.entity.Stats;
import sdcc.surveyshub.model.entity.Survey;
import sdcc.surveyshub.model.mapper.StatsMapper;
import sdcc.surveyshub.model.record.User;
import sdcc.surveyshub.model.record.response.StatsResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final SurveyDao surveyDao;

    private final QuestionDao questionDao;

    private final StatsDao statsDao;

    private final StatsMapper statsMapper;

    @Override
    public Stats createStatsSubCollections(Survey survey, List<Question> questions) {
        Stats s = new Stats();
        s.setId(survey.getId());
        s.setTotalSubmitted(0L);
        s.setFirstSubmitted(null);
        s.setLastSubmitted(null);

        Map<String, Map<String, Long>> questionStats = new HashMap<>();
        for( Question q : questions ) {
            Map<String, Long> choices = new HashMap<>();
            for ( QuestionChoice qc : q.getQuestionChoices() ) {
                choices.put(String.valueOf(qc.getNumSequence()), 0L);
            }
            questionStats.put(String.valueOf(q.getNumSequence()), choices);
        }
        s.setQuestionStats(questionStats);
        statsDao.create(s);
        return s;
    }

    @Override
    public StatsResponse getStatsFromSurvey(String surveyId, User user) {
        List<Question> q = questionDao.findAllBySurveyId(surveyId);
        Stats s = statsDao.findBySurveyId(surveyId);
        if (s == null) {
            throw new NotFoundException("Stats non trovate per survey " + surveyId);
        }
        return statsMapper.toResponse(s, q);
    }

    @Override
    public StatsResponse getStatsFromPublicSurvey(String surveyId) {
        List<Question> q = questionDao.findAllBySurveyId(surveyId);
        Stats s = statsDao.findBySurveyId(surveyId);
        if (s == null) {
            throw new NotFoundException("Stats non trovate per survey " + surveyId);
        }
        return statsMapper.toResponse(s, q);
    }

}