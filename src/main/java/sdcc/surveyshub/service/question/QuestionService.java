package sdcc.surveyshub.service.question;

import sdcc.surveyshub.model.record.User;
import sdcc.surveyshub.model.record.response.QuestionResponse;

import java.util.List;

public interface QuestionService {

    List<QuestionResponse> getQuestionsFromSurvey(String surveyId, User user);

    List<QuestionResponse> getQuestionsFromPublicSurvey(String surveyId);

}
