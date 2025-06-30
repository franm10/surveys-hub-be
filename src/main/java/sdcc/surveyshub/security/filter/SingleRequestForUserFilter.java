package sdcc.surveyshub.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sdcc.surveyshub.security.user.UserPrincipal;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SingleRequestForUserFilter extends OncePerRequestFilter {

    private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                                                    throws ServletException, IOException {
        Object principal = request.getUserPrincipal();
        if( !(principal instanceof UserPrincipal user) ) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = user.getUid();

        // Only one request at time for user
        if (!inFlight.add(userId)) {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Only one request at a time allowed per user");
            return;
        }

        try {
            filterChain.doFilter(request, response);
        }finally {
            inFlight.remove(userId);
        }
    }
}
