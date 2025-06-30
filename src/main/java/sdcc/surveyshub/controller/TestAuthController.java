package sdcc.surveyshub.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestAuthController {

    @GetMapping("/auth/test")
    public ResponseEntity<?> authTestHelloWorld() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("message", "Richiesta autenticata andata a buon fine",
                                "data", "Hello World by Surveys Hub!"));
    }

    @GetMapping("/admin/test")
    public ResponseEntity<?> adminTestHelloWorld() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("message", "Richiesta autenticata ADMIN andata a buon fine",
                        "data", "Hello World by Surveys Hub!"));
    }

    @GetMapping("/user/test")
    public ResponseEntity<?> userTestHelloWorld() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("message", "Richiesta autenticata USER andata a buon fine",
                        "data", "Hello World by Surveys Hub!"));
    }

    @GetMapping("/public/test")
    public ResponseEntity<?> testHelloWorld() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("message", "Richiesta non autenticata andata a buon fine",
                        "data", "Hello World by Surveys Hub!"));
    }

}
