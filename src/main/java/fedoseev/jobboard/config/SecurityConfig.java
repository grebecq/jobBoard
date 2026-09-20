package fedoseev.jobboard.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@EnableMethodSecurity
@Configuration
@EnableWebSecurity
public class SecurityConfig {

   private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // до общих правил, иначе попадут под permitAll
                        .requestMatchers(HttpMethod.GET, "/api/vacancies/my").hasAnyRole("EMPLOYER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/companies/my").hasAnyRole("EMPLOYER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/vacancies/**", "/api/companies/**").permitAll()

                        .requestMatchers(HttpMethod.POST,   "/api/companies/**").hasAnyRole("EMPLOYER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/companies/**").hasAnyRole("EMPLOYER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/companies/**").hasAnyRole("EMPLOYER", "ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/vacancies/**").hasAnyRole("EMPLOYER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/vacancies/**").hasAnyRole("EMPLOYER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/vacancies/**").hasAnyRole("EMPLOYER", "ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/applications/apply/**").hasRole("CANDIDATE")

                        .requestMatchers("/api/**").authenticated()

                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) ->
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized", "Требуется вход");
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Forbidden", "Недостаточно прав");
    }

    private void writeError(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"error\":\"" + error + "\","
                        + "\"message\":\"" + message + "\","
                        + "\"timestamp\":\"" + LocalDateTime.now() + "\"}");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
