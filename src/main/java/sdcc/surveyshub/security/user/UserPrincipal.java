package sdcc.surveyshub.security.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
public class UserPrincipal implements Serializable {

    private String uid;
    private String email;
    private String name;
    private String role;

    public Collection<? extends GrantedAuthority> toAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

}
