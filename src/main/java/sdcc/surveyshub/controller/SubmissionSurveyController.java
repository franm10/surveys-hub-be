package sdcc.surveyshub.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sdcc.surveyshub.exception.InvalidSubmissionSurveyException;
import sdcc.surveyshub.model.mapper.UserMapper;
import sdcc.surveyshub.model.record.request.SubmissionSurveyRequest;
import sdcc.surveyshub.model.record.response.ApiResponse;
import sdcc.surveyshub.model.record.response.SubmissionSurveyCompleteResponse;
import sdcc.surveyshub.model.record.response.SubmissionSurveyResponse;
import sdcc.surveyshub.security.user.UserPrincipal;
import sdcc.surveyshub.service.submission.SubmissionSurveyService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user/surveys")
public class SubmissionSurveyController {

    private final SubmissionSurveyService subsService;

    private final UserMapper userMapper;

    public SubmissionSurveyController(SubmissionSurveyService subsService, UserMapper userMapper) {
        this.subsService = subsService;
        this.userMapper = userMapper;
    }

    @GetMapping("/{surveyId}/submitted/users-list")
    public ResponseEntity<ApiResponse<?>> getParticipatedUserList(@PathVariable String surveyId, @AuthenticationPrincipal UserPrincipal owner) {
        log.info("[SurveyAPI][getParticipatedUserList] Request to get all emails to response to survey {}", surveyId);
        List<String> users = subsService.getParticipatedUserFromSurvey(surveyId, userMapper.toRecord(owner));
        return ResponseEntity.ok(ApiResponse.build("200 OK", users));
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<?>> submit(@Valid @RequestBody SubmissionSurveyRequest request, @AuthenticationPrincipal UserPrincipal user) {
        log.info("[SubmissionSurveyAPI][submit] Request to submit answers for survey {} from user {}", request.surveyId(), user.getEmail());
        try {
            SubmissionSurveyResponse ssr = subsService.submit(request, userMapper.toRecord(user));
            return ResponseEntity.ok(ApiResponse.build("200 OK", ssr));
        }catch( InvalidSubmissionSurveyException e ) {
            return ResponseEntity.badRequest().body(ApiResponse.build("400 BAD REQUEST", e.getMessage()));
        }
    }

    @GetMapping("/submitted-by-user")
    public ResponseEntity<ApiResponse<?>> getAllResponseFromUser(@AuthenticationPrincipal UserPrincipal user) {
        log.info("[SubmissionSurveyAPI][getAllResponseFromUser] Request to check answers from user {}", user.getEmail());
        List<String> response = subsService.getAllResponseFromUser(userMapper.toRecord(user));
        return ResponseEntity.ok(ApiResponse.build("200 OK", response));
    }

    @GetMapping("/{surveyId}/submitted")
    public ResponseEntity<ApiResponse<?>> getSubmission(@PathVariable String surveyId, @AuthenticationPrincipal UserPrincipal user) {
        log.info("[SubmissionSurveyAPI][getSubmission] Request to get submission from survey {} and user {}", surveyId, user.getEmail());
        SubmissionSurveyResponse ssr = subsService.getSubmission(surveyId, userMapper.toRecord(user));
        return ResponseEntity.ok(ApiResponse.build("200 OK", ssr));
    }

    @GetMapping("/{surveyId}/submitted/complete")
    public ResponseEntity<ApiResponse<?>> getCompleteSubmission(@PathVariable String surveyId, @AuthenticationPrincipal UserPrincipal user) {
        log.info("[SubmissionSurveyAPI][getCompleteSubmission] Survey ID: {}, User: {}", surveyId, user.getEmail());
        SubmissionSurveyCompleteResponse sscr = subsService.getCompleteSubmission(surveyId, userMapper.toRecord(user));
        return ResponseEntity.ok(ApiResponse.build("200 OK", sscr));
    }

}
