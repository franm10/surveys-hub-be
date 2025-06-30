package sdcc.surveyshub.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sdcc.surveyshub.model.record.response.ApiResponse;
import sdcc.surveyshub.model.record.response.QuestionResponse;
import sdcc.surveyshub.model.record.response.StatsResponse;
import sdcc.surveyshub.model.record.response.SurveyResponse;
import sdcc.surveyshub.service.question.QuestionService;
import sdcc.surveyshub.service.stats.StatsService;
import sdcc.surveyshub.service.survey.SurveyService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final SurveyService surveyService;

    private final QuestionService questionService;

    private final StatsService statsService;

    public PublicController(SurveyService surveyService, QuestionService questionService, StatsService statsService) {
        this.surveyService = surveyService;
        this.questionService = questionService;
        this.statsService = statsService;
    }

    @GetMapping("/surveys/get-all")
    public ResponseEntity<ApiResponse<?>> getAllPublicSurveys() {
        log.info("[SurveyAPI][getAllPublicSurveys] Request to get all public surveys");
        List<SurveyResponse> surveys = surveyService.getAllPublicSurveys();
        return ResponseEntity.ok(ApiResponse.build("200 OK", surveys));
    }

    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<?>> getQuestionsFromPublicSurvey(@RequestParam(name="survey") String surveyId) {
        log.info("[SurveyAPI][getQuestionsFromPublicSurvey] Request to get all questions from surveys {}", surveyId);
        List<QuestionResponse> surveys = questionService.getQuestionsFromPublicSurvey(surveyId);
        return ResponseEntity.ok(ApiResponse.build("200 OK", surveys));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<?>> getStatsFromPublicSurvey(@RequestParam(name="survey") String surveyId) {
        log.info("[SurveyAPI][getStatsFromPublicSurvey] Request to get stats from surveys {}", surveyId);
        StatsResponse stats = statsService.getStatsFromPublicSurvey(surveyId);
        return ResponseEntity.ok(ApiResponse.build("200 OK", stats));
    }

}
