package helen.com.authservice.config;

import helen.com.authservice.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    // Endpoints that don't require a valid access token.
    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/**",
            "/password/**",
            // Called mid-login (AuthService.login returns mfaRequired=true before
            // any JWT is issued) - must stay public. /mfa/setup and /mfa/enable
            // are NOT here on purpose: they require an authenticated user.
            "/mfa/verify",
            "/health",
            "/actuator/**",
            "/oauth2/**",
            "/login/oauth2/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain security(HttpSecurity http) throws Exception {
         http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                // IF_REQUIRED (not STATELESS): the JWT-authenticated API calls never
                // trigger a session, but the OAuth2 login redirect dance (state/nonce
                // stored by Spring Security between the /authorization and /callback
                // requests) needs one to exist for that flow only.
                .sessionManagement(
                        session -> session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/oauth2/success", true)
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );
                return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

