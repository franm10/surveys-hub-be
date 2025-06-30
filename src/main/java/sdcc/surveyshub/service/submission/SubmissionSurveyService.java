package sdcc.surveyshub.service.submission;

import sdcc.surveyshub.exception.InvalidSubmissionSurveyException;
import sdcc.surveyshub.model.record.User;
import sdcc.surveyshub.model.record.request.SubmissionSurveyRequest;
import sdcc.surveyshub.model.record.response.SubmissionSurveyCompleteResponse;
import sdcc.surveyshub.model.record.response.SubmissionSurveyResponse;

import java.util.List;

public interface SubmissionSurveyService {

    List<String> getParticipatedUserFromSurvey(String surveyId, User owner);

    SubmissionSurveyResponse submit(SubmissionSurveyRequest submissionSurveyRequest, User user) throws InvalidSubmissionSurveyException;

    List<String> getAllResponseFromUser(User user);

    SubmissionSurveyResponse getSubmission(String surveyId, User user);

    SubmissionSurveyCompleteResponse getCompleteSubmission(String surveyId, User user);

}