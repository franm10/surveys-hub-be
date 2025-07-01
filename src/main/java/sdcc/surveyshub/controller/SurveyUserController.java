package sdcc.surveyshub.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sdcc.surveyshub.exception.InvalidTokenException;
import sdcc.surveyshub.model.mapper.UserMapper;
import sdcc.surveyshub.model.record.response.ApiResponse;
import sdcc.surveyshub.model.record.response.SurveyResponse;
import sdcc.surveyshub.security.user.UserPrincipal;
import sdcc.surveyshub.service.survey.SurveyService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user/surveys")
public class SurveyUserController {

    private final SurveyService surveyService;

    private final UserMapper userMapper;

    public SurveyUserController(SurveyService surveyService, UserMapper userMapper) {
        this.surveyService = surveyService;
        this.userMapper = userMapper;
    }

    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<?>> inviteByToken(@RequestParam(name="token") String token, @AuthenticationPrincipal UserPrincipal user) {
        log.info("[SurveyAPI][inviteByToken] Try to search survey with token {} from user {}", token, user.getEmail());
        try {
            SurveyResponse s = surveyService.useInviteToken(userMapper.toRecord(user), token);
            if (s != null)
                return ResponseEntity.ok(ApiResponse.build("200 OK", "Request approved.", s));
            return ResponseEntity.ok(ApiResponse.build("200 OK", "Pending request, awaiting owner acceptance."));
        }catch( InvalidTokenException e ) {
            return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.build("400 BAD REQUEST", "Invalid token."));
        }
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<ApiResponse<?>> getSurvey(@PathVariable String surveyId, @AuthenticationPrincipal UserPrincipal user) {
        log.info("[SurveyAPI][getSurvey] Request to get survey {} from user {}", surveyId, user.getEmail());
        SurveyResponse survey = surveyService.getSurvey(surveyId);
        return ResponseEntity.ok(ApiResponse.build("200 OK", survey));
    }

    @GetMapping("/public/get-all")
    public ResponseEntity<ApiResponse<?>> getAllPublicSurveys() {
        log.info("[SurveyAPI][getAllPublicSurveys] Request to get all public surveys");
        List<SurveyResponse> surveys = surveyService.getAllPublicSurveys();
        return ResponseEntity.ok(ApiResponse.build("200 OK", surveys));
    }

    @GetMapping("/invite/get-all")
    public ResponseEntity<ApiResponse<?>> getAllSurveysWhereUserIsInvited(@AuthenticationPrincipal UserPrincipal user) {
        log.info("[SurveyAPI][getAllSurveysWhereUserIsInvited] Request to get all invited surveys from {}", user.getEmail());
        List<SurveyResponse> surveys = surveyService.getAllSurveysWhereUserIsInvited(userMapper.toRecord(user));
        return ResponseEntity.ok(ApiResponse.build("200 OK", surveys));
    }

    @GetMapping("/invite/get-all-open")
    public ResponseEntity<ApiResponse<?>> getAllSurveysOpenWhereUserIsInvited(@AuthenticationPrincipal UserPrincipal user) {
        log.info("[SurveyAPI][getAllSurveysOpenWhereUserIsInvited] Request to get all 'open' invited surveys from {}", user.getEmail());
        List<SurveyResponse> surveys = surveyService.getAllOpenSurveysWhereUserIsInvited(userMapper.toRecord(user));
        return ResponseEntity.ok(ApiResponse.build("200 OK", surveys));
    }

}
