package sdcc.surveyshub.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import jakarta.servlet.http.HttpServletResponse;
import sdcc.surveyshub.security.filter.AuthRequestFilter;
import sdcc.surveyshub.security.filter.SingleRequestForUserFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.client.public.url}")
    private String webAppUrl;

    @Value("{app.client.localhost.url}")
    private String localhost;

    private final AuthRequestFilter authRequestFilter;

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final SingleRequestForUserFilter singleRequestForUserFilter;

    public SecurityConfig(AuthRequestFilter authRequestFilter, SingleRequestForUserFilter singleRequestForUserFilter ) {
        this.authRequestFilter = authRequestFilter;
        this.singleRequestForUserFilter = singleRequestForUserFilter;

    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf( AbstractHttpConfigurer::disable )
                .cors( Customizer.withDefaults() )
                .authorizeHttpRequests( authorize -> authorize
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("admin")
                        .requestMatchers("/api/user/**").hasAuthority("user")
                        .anyRequest().authenticated()
                )
                .exceptionHandling( exception -> exception
                        .authenticationEntryPoint( (request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized") )
                        .accessDeniedHandler((request,response,accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden") )
                )
                .sessionManagement( session -> session
                        .sessionCreationPolicy( SessionCreationPolicy.STATELESS )
                )
                .headers( headers -> headers
                        .frameOptions( HeadersConfigurer.FrameOptionsConfig::deny )
                        .xssProtection(  xss -> xss.headerValue( XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK ) )
                        .contentSecurityPolicy( csp -> csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; object-src 'none';") )
                        .httpStrictTransportSecurity( hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000) )
                )
                .addFilterBefore(authRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(false);
        config.setAllowedOriginPatterns( List.of(webAppUrl, localhost) );
        config.setAllowedHeaders( List.of("Authorization", "Cache-Control", "Content-Type") );
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setExposedHeaders( List.of("Authorization") );

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }

}