package sdcc.surveyshub.model.record.response;

import java.time.Instant;
import java.util.List;

public record SurveyResponse(

        String id,

        String title,
        String description,
        int numberOfQuestions,

        String status,
        Instant expirationDate,

        String visibility,

        Instant createdAt,
        String createdBy,

        String invitedToken,
        boolean approvalRequired,
        List<String> pendingApprovalEmails,

        List<String> invitedEmails

) { }