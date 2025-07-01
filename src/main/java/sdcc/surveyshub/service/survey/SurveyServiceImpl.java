package sdcc.surveyshub.service.survey;

import sdcc.surveyshub.exception.*;
import sdcc.surveyshub.model.record.request.SurveyParamsRequest;
import sdcc.surveyshub.service.stats.StatsService;
import sdcc.surveyshub.utils.BucketUtils;
import sdcc.surveyshub.model.dao.QuestionDao;
import sdcc.surveyshub.model.dao.SubmissionSurveyDao;
import sdcc.surveyshub.model.dao.SurveyDao;
import sdcc.surveyshub.model.entity.QuestionChoice;
import sdcc.surveyshub.model.entity.Question;
import sdcc.surveyshub.model.mapper.QuestionMapper;
import sdcc.surveyshub.model.mapper.SurveyMapper;
import sdcc.surveyshub.model.record.QuestionsBuildResults;
import sdcc.surveyshub.model.record.User;
import sdcc.surveyshub.model.record.request.QuestionChoiceRequest;
import sdcc.surveyshub.model.record.request.QuestionRequest;
import sdcc.surveyshub.model.record.request.SurveyRequest;
import sdcc.surveyshub.model.entity.Survey;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sdcc.surveyshub.model.record.response.SurveyResponse;
import sdcc.surveyshub.service.bucket.BucketServiceImpl;
import sdcc.surveyshub.utils.EmailValidatorUtil;
import sdcc.surveyshub.utils.enums.Status;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService {

    private final SurveyDao surveyDao;

    private final QuestionDao questionDao;

    private final SubmissionSurveyDao submissionSurveyDao;

    private final BucketServiceImpl bucketService;

    private final StatsService statsService;

    private final SurveyMapper surveyMapper;

    private final QuestionMapper questionMapper;


    /// ***************************************************************************
    ///                          OWNER SERVICE METHODS START
    /// ***************************************************************************

    /// *************** CREATED SURVEY END ***************
    public SurveyResponse createSurvey(SurveyRequest req, User owner) throws UploadImageException  {
        Survey s = surveyMapper.toEntity(req, owner.uid(), owner.email());
        surveyDao.save(s);

        QuestionsBuildResults results = buildQuestionsAndImages(s.getId(), req.questions());

        questionDao.saveAll( results.questions() );

        statsService.createStatsSubCollections(s, results.questions());

        if( !results.imagesToUpload().isEmpty() ) {
            try {
                saveAllImages( results.imagesToUpload() );
            }catch( UploadImageException e ) {
                log.warn("[createSurvey] Error to saving image. Rollback survey creation: {}", e.getMessage());
                rollbackSurvey(s.getId());
                throw e;
            }
        }
        return surveyMapper.toResponse(s);
    }

    private QuestionsBuildResults buildQuestionsAndImages(String surveyId, List<QuestionRequest> qReqs) {
        List<Question> questions = new ArrayList<>();
        Map<String,String> imagesToUpload = new HashMap<>();

        int qSeq = 0;
        for( QuestionRequest qr : qReqs ) {
            // Set Question base entity (without img and options)
            Question q = questionMapper.toBaseEntity(qr, surveyId, ++qSeq);

            // Set Question image
            if( qr.imageType()!=null ) {
                if( qr.imageType().equals("URL") )
                    q.setImageUrl(qr.imageData());
                else if( qr.imageType().equals("BASE64") ) {
                    String qPath = BucketUtils.surveyQuestionImgPath(surveyId, qSeq);
                    imagesToUpload.put(qPath, qr.imageData());

                    String absolutePath = BucketUtils.firebaseImageUrl(qPath);
                    q.setImageUrl(absolutePath);
                }
            }

            // Set Question choices
            List<QuestionChoice> questionChoices = new ArrayList<>();
            int qcSeq = 0;
            for( QuestionChoiceRequest qc : qr.options() ) {
                QuestionChoice c = new QuestionChoice();
                c.setNumSequence(++qcSeq);
                c.setText(qc.text());
                if( qc.imageType()!=null ) {
                    if( qc.imageType().equals("URL") )
                        c.setImageUrl(qc.imageData());
                    else if( qc.imageType().equals("BASE64") ) { // imageType == "BASE64"
                        String qcPath = BucketUtils.surveyQuestionChoiceImgPath(surveyId, qSeq, qcSeq);
                        imagesToUpload.put(qcPath, qc.imageData());

                        String absolutePath = BucketUtils.firebaseImageUrl(qcPath);
                        c.setImageUrl(absolutePath);
                    }
                }
                questionChoices.add(c);
            }
            q.setQuestionChoices(questionChoices);

            questions.add(q);
        }
        return new QuestionsBuildResults(questions,imagesToUpload);
    }

    private void saveAllImages(Map<String, String> images) throws UploadImageException {
        for( Map.Entry<String, String> e : images.entrySet() ) {
            String path = e.getKey();
            String base64Data = e.getValue();
            bucketService.uploadBase64Png(path, base64Data);
        }
    }

    private void rollbackSurvey(String surveyId) {
        questionDao.deleteAllFromSurvey(surveyId);
        surveyDao.deleteById(surveyId);
        bucketService.deleteFolder(surveyId);
    }
    /// *************** CREATED SURVEY END ***************

    @Override
    public SurveyResponse updateSettings(String surveyId, SurveyParamsRequest params, User owner) {
        Survey s = surveyDao.findByIdAndOwnerId(surveyId,owner.uid()).orElseThrow(() -> new NotFoundException("Resource not found"));
        if( s.getStatus().equals(Status.ARCHIVED) )
            throw new SurveyStatusException("Cannot change settings of a survey that is archived.");
        if( params.status().equals("open"))
            s.setStatus(Status.OPEN);
        else
            s.setStatus(Status.CLOSED);
        s.setVisibility(params.visibility());
        if(params.expirationDate().isAfter(Instant.now()))
            s.setExpirationDate(params.expirationDate());

        surveyDao.save(s);
        return surveyMapper.toResponse(s);
    }

    @Override
    public void invalidateToken(String surveyId, User owner) {
        Survey s = surveyDao.findByIdAndOwnerId(surveyId, owner.uid()).orElseThrow(() -> new NotFoundException("Resource not found"));
        if( s.getInvitedToken()==null )
            return;
        s.setInvitedToken(null);
        s.setPendingApprovalEmails(new ArrayList<>());
        surveyDao.save(s);
    }

    @Override
    public SurveyResponse generateToken(String surveyId, boolean approvalRequired, User owner) {
        Survey s = surveyDao.findByIdAndOwnerId(surveyId, owner.uid()).orElseThrow(() -> new NotFoundException("Resource not found"));
        if( s.getStatus().equals(Status.CLOSED) || s.getStatus() == Status.ARCHIVED )
            throw new SurveyStatusException("Survey is closed or archived.");

        String token = UUID.randomUUID().toString();
        s.setInvitedToken(token);
        s.setApprovalRequired(approvalRequired);
        s.setPendingApprovalEmails(new ArrayList<>());
        surveyDao.save(s);
        return surveyMapper.toResponse(s);
    }

    @Override
    public void updateApprovalRequired(String surveyId, User owner, boolean approvalRequired) {
        Survey s = surveyDao.findByIdAndOwnerId(surveyId,owner.uid()).orElseThrow(() -> new NotFoundException("Resource not found"));
        if( s.getStatus().equals(Status.CLOSED) || s.getStatus() == Status.ARCHIVED )
            throw new SurveyStatusException("Survey is closed or archived.");
        if( s.getInvitedToken()==null )
            throw new SurveyStatusException("Survey has no invited token.");

        s.setApprovalRequired(approvalRequired);
        if( !approvalRequired )
            s.getInvitedEmails().addAll(s.getPendingApprovalEmails());
        surveyDao.save(s);
    }

    @Override
    public void acceptAllPendingRequest(String surveyId, User owner) {
        Survey s = surveyDao.findByIdAndOwnerId(surveyId, owner.uid()).orElseThrow(() -> new NotFoundException("Resource not found"));
        if( s.getStatus().equals(Status.CLOSED) || s.getStatus() == Status.ARCHIVED )
            throw new SurveyStatusException("Survey is closed or archived.");

        List<String> emails = s.getPendingApprovalEmails();
        for( String email : emails )
            if( !s.getInvitedEmails().contains(email) )
                s.getInvitedEmails().add(email);
        s.setPendingApprovalEmails(new ArrayList<>());
        surveyDao.save(s);
    }

    @Override
    public Set<String> updateInvitedUserFromEmailsList(String surveyId, User owner, Set<String> emails) {
        Survey s = surveyDao.findByIdAndOwnerId(surveyId, owner.uid()).orElseThrow(() -> new NotFoundException("Resource not found"));
        if( s.getStatus().equals(Status.CLOSED) || s.getStatus() == Status.ARCHIVED )
            throw new SurveyStatusException("Survey is closed or archived.");

        s.getPendingApprovalEmails().removeAll(emails);
        Set<String> newEmailsList = new HashSet<>();
        for( String email : emails ) {
            if( EmailValidatorUtil.isValidEmail(email) ) {
                newEmailsList.add(email);
            }
        }
        s.setInvitedEmails(newEmailsList.stream().toList());
        surveyDao.save(s);
        return newEmailsList;
    }

    @Override
    public Set<String> addInvitedUserFromEmailsList(String surveyId, User owner, Set<String> emails) {
        Survey s = surveyDao.findByIdAndOwnerId(surveyId, owner.uid()).orElseThrow(() -> new NotFoundException("Resource not found"));
        if( s.getStatus().equals(Status.CLOSED) || s.getStatus() == Status.ARCHIVED )
            throw new SurveyStatusException("Survey is closed or archived.");

        s.getPendingApprovalEmails().removeAll(emails);
        Set<String> invitedUserEmails = new HashSet<>(s.getInvitedEmails());
        Set<String> addedEmails = new HashSet<>();
        for( String email : emails ) {
            if( EmailValidatorUtil.isValidEmail(email) ) {
                invitedUserEmails.add(email);
                addedEmails.add(email);
            }
        }
        s.setInvitedEmails(invitedUserEmails.stream().toList());
        surveyDao.save(s);
        return addedEmails;
    }

    @Override
    public Set<String> removeInvitedUserFromEmailsList(String surveyId, User owner, Set<String> emails) {
        Survey s = surveyDao.findByIdAndOwnerId(surveyId, owner.uid()).orElseThrow(() -> new NotFoundException("Resource not found"));

        Set<String> removedEmails = new HashSet<>();
        for( String email : emails ) {
            if( submissionSurveyDao.findBySurveyIdAndUserEmail(surveyId, owner.email()).isEmpty() ) {
                s.getInvitedEmails().remove(email);
                removedEmails.add(email);
            }
        }
        surveyDao.save(s);
        return removedEmails;
    }

    public SurveyResponse getSurveyByOwnerId(String surveyId, User owner) {
        Survey s = surveyDao.findByIdAndOwnerId(surveyId, owner.uid()).orElseThrow(() -> new NotFoundException("Resource not found"));
        return surveyMapper.toResponse(s);
    }

    @Override
    public List<SurveyResponse> getAllSurveysByOwnerId(User owner) {
        return surveyDao.findAllByOwnerId(owner.uid())
                        .stream()
                        .map(surveyMapper::toResponse)
                        .toList();
    }

    /// ***************************************************************************
    ///                         OWNER SERVICE METHODS END
    /// ***************************************************************************

    /// ***************************************************************************
    ///                         USER SERVICE METHODS START
    /// ***************************************************************************
    @Override
    public SurveyResponse useInviteToken(User user, String token) throws InvalidTokenException, DuplicatedRequestException {
        Survey s = surveyDao.findByToken(token).orElseThrow(() -> new InvalidTokenException("Invalid token."));
        if( s.getStatus().equals(Status.CLOSED) || s.getStatus() == Status.ARCHIVED )
            throw new SurveyStatusException("Survey is closed or archived.");

        if( s.getPendingApprovalEmails().contains(user.email()) )
            throw new DuplicatedRequestException("User has already requested to join this survey. Please await owner acceptance.");

        if( s.getInvitedEmails().contains(user.email()) )
            throw new DuplicatedRequestException("User is already invited and can respond to the survey.");

        if( s.isApprovalRequired() ) {
            s.getPendingApprovalEmails().add(user.email());
            surveyDao.save(s);
            return null;
        }
        s.getInvitedEmails().add(user.email());
        surveyDao.save(s);
        return surveyMapper.toResponseWithoutSensitiveData(s);
    }

    @Override
    public List<SurveyResponse> getAllSurveysWhereUserIsInvited(User user) {
        return surveyDao.findAllWhereUserIsInvited(user.email())
                        .stream()
                        .map(surveyMapper::toResponseWithoutSensitiveData)
                        .toList();
    }

    @Override
    public List<SurveyResponse> getAllSurveysWhereUserResponse(User user) {
        return surveyDao.findAllWithSubmissionByUser(user.uid())
                .stream()
                .map(surveyMapper::toResponseWithoutSensitiveData)
                .toList();
    }

    @Override
    public List<SurveyResponse> getAllOpenSurveysWhereUserIsInvited(User user) {
        return surveyDao.findAllWhereUserIsInvitedAndIsOpen(user.email())
                .stream()
                .map(surveyMapper::toResponseWithoutSensitiveData)
                .toList();
    }

    @Override
    public List<SurveyResponse> getAllPublicSurveys() {
        return surveyDao.findAllWithPublicVisibility()
                        .stream()
                        .map(surveyMapper::toResponseWithoutSensitiveData)
                        .toList();
    }

    @Override
    public SurveyResponse getSurvey(String surveyId) {
        return surveyMapper.toResponseWithoutSensitiveData(surveyDao.findById(surveyId).orElseThrow(() -> new NotFoundException("Resource not found")));
    }

/// **************************************************
///         METHOD: ADMIN GET ALL SURVEYS
/// **************************************************

    @Override
    public List<SurveyResponse> getAllSurveys() {
        return surveyDao.findAll()
                .stream()
                .map(surveyMapper::toResponseWithoutSensitiveData)
                .toList();
    }

    @Override
    public void deleteSurvey(String surveyId) {
        Survey s = surveyDao.findById(surveyId).orElseThrow(() -> new NotFoundException("Resource not found"));
        surveyDao.deleteById(surveyId);
    }

}