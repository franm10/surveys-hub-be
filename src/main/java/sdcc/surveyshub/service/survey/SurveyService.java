package sdcc.surveyshub.service.survey;

import sdcc.surveyshub.exception.DuplicatedRequestException;
import sdcc.surveyshub.exception.InvalidTokenException;
import sdcc.surveyshub.exception.UploadImageException;
import sdcc.surveyshub.model.record.User;
import sdcc.surveyshub.model.record.request.SurveyParamsRequest;
import sdcc.surveyshub.model.record.request.SurveyRequest;
import sdcc.surveyshub.model.record.response.SurveyResponse;

import java.util.List;
import java.util.Set;

@SuppressWarnings("UnusedReturnValue")
public interface SurveyService {

/// OWNER SERVICE START
    SurveyResponse createSurvey(SurveyRequest req, User owner) throws UploadImageException;

    SurveyResponse updateSettings(String surveyId, SurveyParamsRequest req, User owner);

    void invalidateToken(String surveyId, User owner);

    SurveyResponse generateToken(String surveyId, boolean approvalRequired, User owner);

    void updateApprovalRequired(String surveyId, User owner, boolean approvalRequired);

    void acceptAllPendingRequest(String surveyId, User owner);

    Set<String> updateInvitedUserFromEmailsList(String surveyId, User owner, Set<String> emails);

    Set<String> addInvitedUserFromEmailsList(String surveyId, User owner, Set<String> emails);

    Set<String> removeInvitedUserFromEmailsList(String surveyId, User owner, Set<String> emails);

    SurveyResponse getSurveyByOwnerId(String surveyId, User owner);

    List<SurveyResponse> getAllSurveysByOwnerId(User owner);
/// OWNER SERVICE END

/// USER SERVICE START
    SurveyResponse useInviteToken(User user, String token) throws InvalidTokenException, DuplicatedRequestException;

    List<SurveyResponse> getAllSurveysWhereUserIsInvited(User user);

    List<SurveyResponse> getAllSurveysWhereUserResponse(User user);

    List<SurveyResponse> getAllOpenSurveysWhereUserIsInvited(User user);

    List<SurveyResponse> getAllPublicSurveys();

    SurveyResponse getSurvey(String surveyId);

/// ADMIN SERVICE
    List<SurveyResponse> getAllSurveys();

    void deleteSurvey(String surveyId);
}
