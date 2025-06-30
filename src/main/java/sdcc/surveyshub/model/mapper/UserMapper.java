package sdcc.surveyshub.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sdcc.surveyshub.model.record.User;
import sdcc.surveyshub.security.user.UserPrincipal;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "uid", source = "userPrincipal.uid")
    @Mapping(target = "name", source = "userPrincipal.name")
    @Mapping(target = "email", source = "userPrincipal.email")
    User toRecord(UserPrincipal userPrincipal);

}
