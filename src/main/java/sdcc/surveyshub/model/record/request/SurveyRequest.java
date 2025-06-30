package sdcc.surveyshub.model.record.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.List;

public record SurveyRequest(

        @NotNull @NotEmpty  String title,
        @NotNull @NotEmpty  String description,
        @NotNull @NotEmpty  List<QuestionRequest> questions,

                            Instant expirationDate,
                            String visibility,

                            boolean generateInvitedToken,
                            boolean approvalRequired,

                            List<String> invitedEmails

) {}