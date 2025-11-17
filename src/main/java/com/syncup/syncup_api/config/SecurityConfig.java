package com.syncup.syncup_api.config;

import com.syncup.syncup_api.dto.LoginResponse;
import com.syncup.syncup_api.security.CustomOAuth2UserService;
import com.syncup.syncup_api.security.TokenAuthenticationFilter;
import com.syncup.syncup_api.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; // <--- Asegúrate que este import existe
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private UsuarioService usuarioService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // <--- AQUÍ
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**", "/login/oauth2/code/google", "/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(authenticationSuccessHandler())
            );
        
        http.addFilterBefore(
            new TokenAuthenticationFilter(usuarioService), 
            UsernamePasswordAuthenticationFilter.class
        );

        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
            String email = oauthUser.getAttribute("email");
            String name = oauthUser.getAttribute("name");

            LoginResponse loginResponse = usuarioService.processGoogleLogin(email, name);
            String token = loginResponse.getToken();
            String nombre = loginResponse.getNombre();
            boolean onboardingStatus = loginResponse.isHaCompletadoOnboarding();
            
            String encodedName = java.net.URLEncoder.encode(nombre, java.nio.charset.StandardCharsets.UTF_8.toString());

            String targetUrl = "http://localhost:4200/login-success?token=" + token + "&name=" + encodedName + "&onboarding=" + onboardingStatus;
            response.sendRedirect(targetUrl);
        };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}