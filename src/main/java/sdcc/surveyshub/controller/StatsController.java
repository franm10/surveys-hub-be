package sdcc.surveyshub.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sdcc.surveyshub.model.mapper.UserMapper;
import sdcc.surveyshub.model.record.response.ApiResponse;
import sdcc.surveyshub.model.record.response.StatsResponse;
import sdcc.surveyshub.security.user.UserPrincipal;
import sdcc.surveyshub.service.stats.StatsService;

@Slf4j
@RestController
@RequestMapping("/api/user/stats")
public class StatsController {

    private final StatsService statsService;

    private final UserMapper userMapper;

    public StatsController(StatsService statsService, UserMapper userMapper) {
        this.statsService = statsService;
        this.userMapper = userMapper;
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<?>> getStatsFromSurvey(@RequestParam(name="survey") String surveyId, @AuthenticationPrincipal UserPrincipal user) {
        log.info("[SurveyAPI][getStatsFromSurvey] Request to get stats from survey {} by user {}", surveyId, user.getEmail());
        StatsResponse stats = statsService.getStatsFromSurvey(surveyId, userMapper.toRecord(user));
        return ResponseEntity.ok(ApiResponse.build("200 OK", stats));
    }

}
