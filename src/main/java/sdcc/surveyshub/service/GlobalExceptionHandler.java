package sdcc.surveyshub.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import sdcc.surveyshub.exception.*;
import sdcc.surveyshub.model.record.response.ApiResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
           IMPORTANT: AccessDeniedException defined in Security Config
     */

    /**
     *  Used by Fallback Controller:
     *  e.getMessage --> Unknown endpoint <api-rest-endpoint>
     * */
    @ExceptionHandler(UnknownEndpointException.class)
    public ResponseEntity<ApiResponse<?>> handleUnknownEndpoint(UnknownEndpointException e) {
        log.warn("[ExceptionHandler] UnknownEndpointException: {}", e.getMessage());
        return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.build("404 NOT FOUND", e.getMessage()));
    }

    /**
     *      Used by @Valid Annotation -> Json validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("[ExceptionHandler] MethodArgumentNotValidException: {}", e.getMessage());
        return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.build("400 BAD REQUEST", "Invalid json body request."));
    }

    /**
     *      Used by find/save on Firestore collections
     * */
    @ExceptionHandler(FirestoreIOException.class)
    public ResponseEntity<ApiResponse<?>> handleFirestoreIOException(FirestoreIOException e) {
        log.warn("[ExceptionHandler] FirestoreIOException: {}", e.getMessage());
        return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(ApiResponse.build("503 SERVICE UNAVAILABLE", "Unexpected error from Firestore. Please try again later."));
    }

    /**
     *      Used by find on Firestore collections
     * */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFoundException(NotFoundException e) {
        log.warn("[ExceptionHandler] NotFoundException: {}", e.getMessage());
        return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.build("404 NOT FOUND", "Resource not found."));
    }

    /**
     *      Used when try to access to closed or archived survey
     */
    @ExceptionHandler(SurveyStatusException.class)
    public ResponseEntity<ApiResponse<?>> handleSurveyStatusException(SurveyStatusException e) {
        log.warn("[ExceptionHandler] SurveyStatusException: {}", e.getMessage());
        return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(ApiResponse.build("409 CONFLICT", e.getMessage()));
    }

    /**
     *      Used when try to access to closed or archived survey
     */
    @ExceptionHandler(DuplicatedRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicatedRequestException(DuplicatedRequestException e) {
        log.warn("[ExceptionHandler] DuplicatedRequestException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.build("422 UNPROCESSABLE ENTITY", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("[ExceptionHandler] IllegalArgumentException: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.build("400 BAD REQUEST", e.getMessage()));
    }

    /**
     *      Generic Exception
     * */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAnyException(Exception e) {
        log.warn("[ExceptionHandler] Generic Exception: {}", e.getMessage());
        return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.build("500 INTERNAL SERVER ERROR", "Unexpected internal error. Please try again later."));
    }

}