package sdcc.surveyshub.controller.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sdcc.surveyshub.model.mapper.UserMapper;
import sdcc.surveyshub.model.record.response.ApiResponse;
import sdcc.surveyshub.model.record.response.QuestionResponse;
import sdcc.surveyshub.model.record.response.StatsResponse;
import sdcc.surveyshub.model.record.response.SurveyResponse;
import sdcc.surveyshub.security.user.UserPrincipal;
import sdcc.surveyshub.service.question.QuestionService;
import sdcc.surveyshub.service.stats.StatsService;
import sdcc.surveyshub.service.survey.SurveyService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SurveyService surveyService;

    private final QuestionService questionService;

    private final StatsService statsService;

    public AdminController(SurveyService surveyService, QuestionService questionService, StatsService statsService) {
        this.surveyService = surveyService;
        this.questionService = questionService;
        this.statsService = statsService;
    }

    @GetMapping("/surveys/{surveyId}")
    public ResponseEntity<ApiResponse<?>> getSurvey(@PathVariable String surveyId) {
        log.info("[SurveyAPI][getSurvey] Request to get survey {} from admin", surveyId);
        SurveyResponse survey = surveyService.getSurvey(surveyId);
        return ResponseEntity.ok(ApiResponse.build("200 OK", survey));
    }

    @DeleteMapping("/surveys/{surveyId}")
    public ResponseEntity<ApiResponse<?>> deleteSurvey(@PathVariable String surveyId) {
        log.info("[SurveyAPI][getSurvey] Request to delete survey {} from admin", surveyId);
        surveyService.deleteSurvey(surveyId);
        return ResponseEntity.ok(ApiResponse.build("200 OK", null));
    }

    @GetMapping("/surveys/get-all")
    public ResponseEntity<ApiResponse<?>> getAllSurveys() {
        log.info("[SurveyAPI][getAllPublicSurveys] Request to get all surveys from admin");
        List<SurveyResponse> surveys = surveyService.getAllSurveys();
        return ResponseEntity.ok(ApiResponse.build("200 OK", surveys));
    }

    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<?>> getQuestionsFromSurvey(@RequestParam(name="survey") String surveyId) {
        log.info("[SurveyAPI][getQuestionsFromSurvey] Request to get questions from survey {} by admin", surveyId);
        List<QuestionResponse> surveys = questionService.getQuestionsFromSurveyByAdmin(surveyId);
        return ResponseEntity.ok(ApiResponse.build("200 OK", surveys));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<?>> getStatsFromSurvey(@RequestParam(name="survey") String surveyId) {
        log.info("[SurveyAPI][getStatsFromSurvey] Request to get stats from survey {} by admin", surveyId);
        StatsResponse stats = statsService.getStatsFromSurveyByAdmin(surveyId);
        return ResponseEntity.ok(ApiResponse.build("200 OK", stats));
    }

}