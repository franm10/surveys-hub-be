package sdcc.surveyshub.model.entity;

import lombok.NoArgsConstructor;

import lombok.Data;
import sdcc.surveyshub.utils.enums.Status;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class Survey {

    private String id;

    private String title;
    private String description;
    private int numberOfQuestions;

    private Status status;
    private Instant expirationDate;

    private String visibility;      //public or private

    private String ownerId;
    private String createdBy;       //ownerId.email
    private Instant createdAt;

    private String invitedToken;
    private boolean approvalRequired;
    private List<String> pendingApprovalEmails;

    private List<String> invitedEmails;

}
