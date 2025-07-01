package sdcc.surveyshub.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sdcc.surveyshub.exception.UploadImageException;
import sdcc.surveyshub.model.mapper.UserMapper;
import sdcc.surveyshub.model.record.request.SurveyParamsRequest;
import sdcc.surveyshub.model.record.request.SurveyRequest;
import sdcc.surveyshub.model.record.response.ApiResponse;
import sdcc.surveyshub.model.record.response.SurveyResponse;
import sdcc.surveyshub.security.user.UserPrincipal;
import sdcc.surveyshub.service.survey.SurveyService;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/user/my-surveys")
public class SurveyOwnerController {

    private final SurveyService surveyService;

    private final UserMapper userMapper;

    public SurveyOwnerController(SurveyService surveyService, UserMapper userMapper) {
        this.surveyService = surveyService;
        this.userMapper = userMapper;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createSurvey(@Valid @RequestBody SurveyRequest surveyRequest, @AuthenticationPrincipal UserPrincipal user) {
        log.info("[SurveyAPI][createSurvey] Request to create new survey from user {}", user.getEmail());
        try {
            SurveyResponse survey = surveyService.createSurvey(surveyRequest, userMapper.toRecord(user));
            return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(ApiResponse.build("203 CREATED", survey));
        }catch( UploadImageException e ) {
            return ResponseEntity
                            .status(HttpStatus.BAD_GATEWAY)
                            .body(ApiResponse.build("502 BAD GATEWAY", e.getMessage()));
        }
    }

    @PatchMapping("/{surveyId}/settings")
    public ResponseEntity<ApiResponse<?>> updateSettings(@PathVariable String surveyId, @Valid @RequestBody SurveyParamsRequest params, @AuthenticationPrincipal UserPrincipal owner) {
        log.info("[SurveyAPI][updateSettings] Request to update settings of survey: {}, params: {}", surveyId, params);
        SurveyResponse survey = surveyService.updateSettings(surveyId, params, userMapper.toRecord(owner));
        return ResponseEntity.ok(ApiResponse.build("200 OK", survey));
    }

    @DeleteMapping("/{surveyId}/token/invalidate")
    public ResponseEntity<ApiResponse<?>> invalidateToken(@PathVariable String surveyId, @AuthenticationPrincipal UserPrincipal owner) {
        log.info("[SurveyAPI][invalidateToken] Request to invalidate token for survey {}", surveyId);
        surveyService.invalidateToken(surveyId, userMapper.toRecord(owner));
        return ResponseEntity.ok(ApiResponse.build("200 OK", "Invalidated token successfully"));
    }

    @PutMapping("/{surveyId}/token/generate")
    public ResponseEntity<ApiResponse<?>> generateToken(@PathVariable String surveyId, @RequestParam(name="approvalRequired", required=false, defaultValue="false") boolean approvalRequired, @AuthenticationPrincipal UserPrincipal owner) {
        log.info("[SurveyAPI][generateNewToken] Request to generate new token for survey {}", surveyId);
        SurveyResponse survey = surveyService.generateToken(surveyId, approvalRequired, userMapper.toRecord(owner));
        return ResponseEntity.ok(ApiResponse.build("200 OK", survey));
    }

    @PatchMapping("/{surveyId}/token")
    public ResponseEntity<ApiResponse<?>> updateApprovalRequired(@PathVariable String surveyId, @RequestParam(name="approvalRequired") boolean approvalRequired, @AuthenticationPrincipal UserPrincipal owner) {
        log.info("[SurveyAPI][updateApprovalRequired] Request to set approval required to survey {}", surveyId);
        surveyService.updateApprovalRequired(surveyId, userMapper.toRecord(owner), approvalRequired);
        return ResponseEntity.ok(ApiResponse.build("200 OK", "Survey updated successfully"));
    }

    @PatchMapping("/{surveyId}/token/accept-pending-requests")
    public ResponseEntity<ApiResponse<?>> acceptAllPendingEmails(@PathVariable String surveyId, @AuthenticationPrincipal UserPrincipal owner) {
        log.info("[SurveyAPI][acceptALlPendingEmails] Request to accept all pending emails to survey {}", surveyId);
        surveyService.acceptAllPendingRequest(surveyId, userMapper.toRecord(owner));
        return ResponseEntity.ok(ApiResponse.build("200 OK", "All pending requests accepted"));
    }

    @PostMapping("/{surveyId}/update-invited-emails")
    public ResponseEntity<ApiResponse<?>> updateInvitedEmails(@PathVariable String surveyId, @RequestBody Set<String> emails, @AuthenticationPrincipal UserPrincipal owner) {
        log.info("[SurveyAPI][addInvitedEmails] Request to update invited emails to survey {}", surveyId);
        Set<String> updatedEmails = surveyService.updateInvitedUserFromEmailsList(surveyId, userMapper.toRecord(owner), emails);
        Set<String> failedEmails = new HashSet<>(emails);
        failedEmails.removeAll(updatedEmails);
        if(failedEmails.isEmpty())     // tutti aggiunti
            return ResponseEntity.ok(ApiResponse.build("200 OK", Map.of("success", updatedEmails)));
        else {
            String message = String.format("Failed to add %d email(s) to survey.", failedEmails.size());
            return ResponseEntity
                    .status(HttpStatus.MULTI_STATUS)
                    .body(ApiResponse.build("207 MULTI STATUS", message, Map.of("success", updatedEmails, "failed", failedEmails)));
        }
    }

    @PostMapping("/{surveyId}/add-invited-emails")
    public ResponseEntity<ApiResponse<?>> addInvitedEmails(@PathVariable String surveyId, @RequestBody Set<String> emails, @AuthenticationPrincipal UserPrincipal owner) {
        log.info("[SurveyAPI][addInvitedEmails] Request to add invited emails to survey {}", surveyId);
        Set<String> addedEmails = surveyService.addInvitedUserFromEmailsList(surveyId, userMapper.toRecord(owner), emails);
        Set<String> failedEmails = new HashSet<>(emails);
        failedEmails.removeAll(addedEmails);
        if(failedEmails.isEmpty())     // tutti aggiunti
            return ResponseEntity.ok(ApiResponse.build("200 OK", Map.of("added", addedEmails)));
        else {
            String message = String.format("Failed to add %d email(s) to survey.", failedEmails.size());
            return ResponseEntity
                            .status(HttpStatus.MULTI_STATUS)
                            .body(ApiResponse.build("207 MULTI STATUS", message, Map.of("added", addedEmails, "failed", failedEmails)));
        }
    }

    @DeleteMapping("/{surveyId}/remove-invited-emails")
    public ResponseEntity<ApiResponse<?>> removeInvitedEmails(@PathVariable String surveyId, @RequestBody Set<String> emails, @AuthenticationPrincipal UserPrincipal owner) {
        log.info("[SurveyAPI][removeInvitedEmails] Request to remove invited emails to survey {}", surveyId);
        Set<String> removedEmails = surveyService.removeInvitedUserFromEmailsList(surveyId, userMapper.toRecord(owner), emails);
        Set<String> failedEmails = new HashSet<>(emails);
        failedEmails.removeAll(removedEmails);
        if(failedEmails.isEmpty())     // tutti rimossi
            return ResponseEntity.ok(ApiResponse.build("200 OK", Map.of("removed", removedEmails)));
        else {
            String message = String.format("Failed to remove %d email(s) to survey.", failedEmails.size());
            return ResponseEntity
                            .status(HttpStatus.MULTI_STATUS)
                            .body(ApiResponse.build("207 MULTI STATUS", message, Map.of("removed", removedEmails, "failed", failedEmails)));
        }
    }

    @GetMapping("/survey")
    public ResponseEntity<ApiResponse<?>> getSurvey(@RequestParam(name="id") String surveyId, @AuthenticationPrincipal UserPrincipal owner) {
        log.info("[SurveyAPI][getSurvey] Request to get survey {} by owner {}", surveyId, owner.getEmail());
        SurveyResponse survey = surveyService.getSurveyByOwnerId(surveyId, userMapper.toRecord(owner));
        return ResponseEntity.ok(ApiResponse.build("200 OK", survey));
    }

    @GetMapping("/all-by-owner")
    public ResponseEntity<ApiResponse<?>> getAllMySurveys(@AuthenticationPrincipal UserPrincipal owner) {
        log.info("[SurveyAPI][getAllMySurveys] Request to get all surveys published by owner {}", owner.getEmail());
        List<SurveyResponse> surveys = surveyService.getAllSurveysByOwnerId(userMapper.toRecord(owner));
        return ResponseEntity.ok(ApiResponse.build("200 OK", surveys));
    }

}