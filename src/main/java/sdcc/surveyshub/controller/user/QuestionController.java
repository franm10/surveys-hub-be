package sdcc.surveyshub.controller.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sdcc.surveyshub.model.mapper.UserMapper;
import sdcc.surveyshub.model.record.response.ApiResponse;
import sdcc.surveyshub.model.record.response.QuestionResponse;
import sdcc.surveyshub.security.user.UserPrincipal;
import sdcc.surveyshub.service.question.QuestionService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user/questions")
public class QuestionController {

    private final QuestionService questionService;

    private final UserMapper userMapper;

    public QuestionController(QuestionService questionService, UserMapper userMapper) {
        this.questionService = questionService;
        this.userMapper = userMapper;
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<?>> getQuestionsFromSurvey(@RequestParam(name="survey") String surveyId, @AuthenticationPrincipal UserPrincipal user) {
        log.info("[SurveyAPI][getQuestionsFromSurvey] Request to get questions from survey {} by user {}", surveyId, user.getEmail());
        List<QuestionResponse> surveys = questionService.getQuestionsFromSurvey(surveyId, userMapper.toRecord(user));
        return ResponseEntity.ok(ApiResponse.build("200 OK", surveys));
    }

}
