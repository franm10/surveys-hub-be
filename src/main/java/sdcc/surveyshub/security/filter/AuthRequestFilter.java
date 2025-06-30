package sdcc.surveyshub.security.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.FirebaseAuthException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sdcc.surveyshub.security.user.UserPrincipal;

import java.io.IOException;

@Slf4j
@Component
public class AuthRequestFilter extends OncePerRequestFilter {

    private final FirebaseAuth firebaseAuth;

    public AuthRequestFilter(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @SuppressWarnings("ExtractMethodRecommender")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String path = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.debug("[AuthRequestFilter] Received Bearer token in request for {} {}", method, path);
            String idToken = authHeader.substring(7);

            try {
                FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
                String role = String.valueOf(decodedToken.getClaims().getOrDefault("role", "user"));

                UserPrincipal principal = new UserPrincipal(
                                                                    decodedToken.getUid(),
                                                                    decodedToken.getEmail(),
                                                                    decodedToken.getName(),
                                                                    role
                                                            );

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                                                            principal,
                                                                            null,
                                                                            principal.toAuthorities()
                                                                        );
                authentication.setDetails(decodedToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("[AuthRequestFilter] Authenticated user UID={}, email={}, role={}", decodedToken.getUid(), decodedToken.getEmail(), role);

            } catch (FirebaseAuthException e) {
                log.warn("[AuthRequestFilter] Invalid Firebase token: {}", e.getMessage());
            }
        }
        else
            log.debug("[AuthRequestFilter] No Bearer token in request for {} {}", method, path);
        filterChain.doFilter(request, response);
        SecurityContextHolder.clearContext();
    }

}