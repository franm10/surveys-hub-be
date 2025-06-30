package sdcc.surveyshub.service.submission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import sdcc.surveyshub.exception.DuplicatedRequestException;
import sdcc.surveyshub.exception.InvalidSubmissionSurveyException;
import sdcc.surveyshub.exception.NotFoundException;
import sdcc.surveyshub.model.dao.QuestionDao;
import sdcc.surveyshub.model.dao.SubmissionSurveyDao;
import sdcc.surveyshub.model.dao.SurveyDao;
import sdcc.surveyshub.model.entity.*;
import sdcc.surveyshub.model.mapper.QuestionMapper;
import sdcc.surveyshub.model.mapper.SubmissionSurveyMapper;
import sdcc.surveyshub.model.record.User;
import sdcc.surveyshub.model.record.request.SubmissionSurveyRequest;
import sdcc.surveyshub.model.record.response.SubmissionSurveyCompleteResponse;
import sdcc.surveyshub.model.record.response.SubmissionSurveyResponse;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionSurveyServiceImpl implements SubmissionSurveyService {

    private final SurveyDao surveyDao;
    private final QuestionDao questionDao;
    private final SubmissionSurveyDao submissionSurveyDao;

    private final SubmissionSurveyMapper submissionSurveyMapper;
    private final QuestionMapper questionMapper;

    @Override
    public List<String> getParticipatedUserFromSurvey(String surveyId, User owner) {
        Survey s = surveyDao.findByIdAndOwnerId(surveyId, owner.uid()).orElseThrow(() -> new NotFoundException("Resource not found"));
        return submissionSurveyDao.findParticipatedUserEmailsBySurveyId(surveyId);
    }

    @Override
    public SubmissionSurveyResponse submit(SubmissionSurveyRequest request, User user) throws InvalidSubmissionSurveyException {
        Survey s = surveyDao.findById(request.surveyId()).orElseThrow(() -> new NotFoundException("Resource not found"));

        if( !s.getInvitedEmails().contains(user.email()) && !(s.getVisibility().equals("public") && !s.isApprovalRequired()) )
            throw new AccessDeniedException("User is not invited to respond.");

        Optional<SubmissionSurvey> opt = submissionSurveyDao.findBySurveyIdAndUserId(request.surveyId(), user.uid());
        if( opt.isPresent() )
            throw new DuplicatedRequestException("User already responded to survey.");

        // Validate Answers --> throws InvalidSubmissionSurveyException
        validateSubmission(questionDao.findAllBySurveyId(request.surveyId()), request.answers());

        SubmissionSurvey ss = submissionSurveyDao.save(submissionSurveyMapper.toEntity(request, user));
        return submissionSurveyMapper.toResponse(ss);
    }

    private void validateSubmission(List<Question> questions, Map<Integer, Set<Integer>> answers) throws InvalidSubmissionSurveyException {
        if( questions.size() != answers.size() )
            throw new InvalidSubmissionSurveyException("Missing answers: expected " + questions.size() + ", send " + answers.size());

        for( Question q : questions ) {
            int qNumSeq = q.getNumSequence();
            Set<Integer> submittedAnswers = answers.get(qNumSeq);
            if( submittedAnswers == null || submittedAnswers.isEmpty() )
                throw new InvalidSubmissionSurveyException("Missing answer for question #"+qNumSeq);
            if( !q.isAllowMultipleAnswers() && submittedAnswers.size()!=1 )
                throw new InvalidSubmissionSurveyException("Question #"+qNumSeq+" cannot accept multiple answers.");

            List<Integer> validChoices = q.getQuestionChoices().stream().map(QuestionChoice::getNumSequence).toList();
            for( Integer choice : submittedAnswers ) {
                if( !validChoices.contains(choice) )
                    throw new InvalidSubmissionSurveyException("Invalid answer choice "+choice+" for question #"+qNumSeq);
            }
        }
    }

    @Override
    public List<String> getAllResponseFromUser(User user) {
        return submissionSurveyDao.findByUserId(user.uid());
    }

    @Override
    public SubmissionSurveyResponse getSubmission(String surveyId, User user) {
        SubmissionSurvey ss = submissionSurveyDao.findBySurveyIdAndUserId(surveyId, user.uid()).orElseThrow(() -> new NotFoundException("Resource not found"));
        return submissionSurveyMapper.toResponse(ss);
    }

    @Override
    public SubmissionSurveyCompleteResponse getCompleteSubmission(String surveyId, User user) {
        SubmissionSurvey submission = submissionSurveyDao.findBySurveyIdAndUserId(surveyId, user.uid()).orElseThrow(() -> new NotFoundException("Resource not found"));
        List<Question> allQuestions = questionDao.findAllBySurveyId(surveyId);
        return submissionSurveyMapper.toCompleteResponse(submission, allQuestions, questionMapper);
    }

}
