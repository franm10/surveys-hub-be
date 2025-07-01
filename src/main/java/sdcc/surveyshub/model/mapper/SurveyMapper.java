package sdcc.surveyshub.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.Named;
import sdcc.surveyshub.model.entity.Survey;
import sdcc.surveyshub.model.record.request.SurveyRequest;
import sdcc.surveyshub.model.record.response.SurveyResponse;
import sdcc.surveyshub.utils.DateUtils;
import sdcc.surveyshub.utils.enums.Status;

import java.time.Instant;
import java.util.*;

@Mapper(componentModel = "spring",
        imports = { UUID.class, Instant.class, DateUtils.class, List.class, ArrayList.class, HashSet.class, Status.class })
public interface SurveyMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "title", source = "req.title")
    @Mapping(target = "description", source = "req.description")
    @Mapping(target = "numberOfQuestions", expression = "java(req.questions().size())")
    @Mapping(target = "status", expression = "java(Status.OPEN)")
    @Mapping(target = "expirationDate", source = "req.expirationDate", qualifiedByName = "checkExpDate")
    @Mapping(target = "visibility", source="req.visibility", qualifiedByName = "checkVisibility")
    @Mapping(target = "ownerId", source = "ownerUid")
    @Mapping(target = "createdBy", source = "ownerEmail")
    @Mapping(target = "createdAt", expression = "java(DateUtils.now())")
    @Mapping(target = "invitedToken", expression = "java(req.generateInvitedToken()? UUID.randomUUID().toString() : null)")
    @Mapping(target = "approvalRequired", source = "req.approvalRequired")
    @Mapping(target = "pendingApprovalEmails", expression = "java(new ArrayList<>())")
    @Mapping(target = "invitedEmails", expression = "java(req.invitedEmails() != null ? new ArrayList<>(new HashSet<>(req.invitedEmails())) : new ArrayList<>())")
    Survey toEntity(SurveyRequest req, String ownerUid, String ownerEmail);

    @Mapping(target = "status", expression = "java(s.getStatus().toString())")
    @Mapping(target = "pendingApprovalEmails", defaultExpression = "java(List.of())")
    @Mapping(target = "invitedEmails", defaultExpression = "java(List.of())")
    SurveyResponse toResponse(Survey s);

    @Mapping(target = "status", expression = "java(s.getStatus().toString())")
    @Mapping(target = "invitedToken", ignore = true)
    @Mapping(target = "pendingApprovalEmails", ignore = true)
    @Mapping(target = "invitedEmails", ignore = true)
    SurveyResponse toResponseWithoutSensitiveData(Survey s);

    @Named("checkExpDate")
    default Instant checkAndGetExpirationDate(Instant expDate) {
        if( expDate == null || expDate.isBefore(DateUtils.now()) )
            return DateUtils.nowPlusYear(1);
        return expDate;
    }

    @Named("checkVisibility")
    default String checkAndGetVisibility(String visibility) {
        if( visibility==null || !(visibility.equals("private") || visibility.equals("public")) )
            return "public";
        return visibility;
    }

}
