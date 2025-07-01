package sdcc.surveyshub.service.question;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import sdcc.surveyshub.exception.NotFoundException;
import sdcc.surveyshub.model.dao.QuestionDao;
import sdcc.surveyshub.model.dao.SurveyDao;
import sdcc.surveyshub.model.entity.Survey;
import sdcc.surveyshub.model.mapper.QuestionMapper;
import sdcc.surveyshub.model.record.User;
import sdcc.surveyshub.model.record.response.QuestionResponse;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final SurveyDao surveyDao;

    private final QuestionDao questionDao;

    private final QuestionMapper questionMapper;

    @Override
    public List<QuestionResponse> getQuestionsFromSurvey(String surveyId, User user) {
        Survey s = surveyDao.findById(surveyId).orElseThrow(() -> new NotFoundException("Resource not found"));
        if( s.getVisibility().equals("private") &&
                !(s.getCreatedBy().equals(user.email()) || s.getInvitedEmails().contains(user.email())) ) {
            log.warn("[IMPORTANT] Intercept unauthorized access to survey: {} from: {}", surveyId, user.email());
            throw new AccessDeniedException("Access denied to survey: "+surveyId);
        }
        return questionDao.findAllBySurveyId(surveyId)
                .stream()
                .map(questionMapper::toResponse)
                .toList();
    }

    @Override
    public List<QuestionResponse> getQuestionsFromPublicSurvey(String surveyId) {
        Survey s = surveyDao.findById(surveyId).orElseThrow(() -> new NotFoundException("Resource not found"));
        if( !s.getVisibility().equals("public") )
            throw new AccessDeniedException("Access denied to survey: "+surveyId);

        return questionDao.findAllBySurveyId(surveyId)
                .stream()
                .map(questionMapper::toResponse)
                .toList();
    }

    @Override
    public List<QuestionResponse> getQuestionsFromSurveyByAdmin(String surveyId) {
        Survey s = surveyDao.findById(surveyId).orElseThrow(() -> new NotFoundException("Resource not found"));

        return questionDao.findAllBySurveyId(surveyId)
                .stream()
                .map(questionMapper::toResponse)
                .toList();
    }

}
