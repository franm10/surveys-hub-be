package sdcc.surveyshub.model.record.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

public record SurveyParamsRequest (

        @Pattern(regexp = "open|closed", message = "Invalid status")
        String status,

        @Pattern(regexp = "public|private", message = "Invalid visibility")
        String visibility,

        @Future(message = "Invalid expiration date")
        Instant expirationDate

) { }
