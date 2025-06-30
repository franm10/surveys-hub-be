package sdcc.surveyshub.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sdcc.surveyshub.exception.UnknownEndpointException;
import sdcc.surveyshub.model.record.response.ApiResponse;

@Slf4j
@RestController
public class FallbackController {

    /** Intercepted by Global Exception Handler */
    @RequestMapping("/**")
    public ResponseEntity<ApiResponse<?>> handleUnknownEndpoint(HttpServletRequest request) {
        log.warn("[***IMPORTANT***][SurveyAPI] Unknown endpoint request received: {}", request.getRequestURI());
        throw new UnknownEndpointException("Unknown endpoint " + request.getRequestURI());
    }

}
