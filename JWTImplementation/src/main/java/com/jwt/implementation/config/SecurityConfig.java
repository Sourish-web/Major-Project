package com.jwt.implementation.config;

import com.jwt.implementation.service.JWTAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/get/profile").permitAll()
                        .requestMatchers("/update/profile").permitAll()
                        .requestMatchers("/profile/password").permitAll()
                        .requestMatchers("/profile/2fa").permitAll()
                        .requestMatchers("/upload/profile/picture").permitAll()
                        .requestMatchers("/remove/profile/picture").permitAll()
                        .requestMatchers("/profile/deactivate").permitAll()
                        .requestMatchers("/delete/profile").permitAll()
                        .requestMatchers("/getUsers").permitAll()
                        .requestMatchers("/transaction/add").authenticated()
                        .requestMatchers("/transaction/all").authenticated()
                        .requestMatchers("/transaction/update").authenticated()
                        .requestMatchers("/transaction/delete/{id}").authenticated()
                        .requestMatchers("/contributeToGoal/{transactionId}/{goalId}").authenticated()
                        .requestMatchers("/addBudget").permitAll()
                        .requestMatchers("/updateBudget").permitAll()
                        .requestMatchers("/getBudget").permitAll()
                        .requestMatchers("/Budget/delete/{id}").permitAll()
                        .requestMatchers("/addAsset").permitAll()
                        .requestMatchers("/getAssets").permitAll()
                        .requestMatchers("/updateAsset").permitAll()
                        .requestMatchers("/deleteAsset/{id}").permitAll()
                        .requestMatchers("/performance").permitAll()
                        .requestMatchers("/portfolio/summary").permitAll()
                        .requestMatchers("/trend").permitAll()
                        .requestMatchers("/portfolio/asset-allocation").permitAll()
                        .requestMatchers("/export/pdf").permitAll()
                        .requestMatchers("/export/csv").permitAll()
                        .requestMatchers("/breakdown/category").permitAll()
                        .requestMatchers("/breakdown/month").permitAll()
                        .requestMatchers("/tax-summary").permitAll()
                        .requestMatchers("/addGoal").permitAll()
                        .requestMatchers("/getGoals").permitAll()
                        .requestMatchers("/updateGoal").permitAll()
                        .requestMatchers("/deleteGoal/{id}").permitAll()
                        .requestMatchers("/allocateToGoal/{id}").permitAll()
                        .requestMatchers("/invite").permitAll()
                        .requestMatchers("/accept-invitation/{invitationId}").permitAll()
                        .requestMatchers("/reject-invitation/{invitationId}").permitAll()
                        .requestMatchers("/my-invitations").permitAll()
                        .requestMatchers("/goalInsights").permitAll()
                        .requestMatchers("/addSubscription").permitAll()
                        .requestMatchers("/getSubscriptions").permitAll()
                        .requestMatchers("/getUpcomingRenewals").permitAll()
                        .requestMatchers("/monthlyCost").permitAll()
                        .requestMatchers("/yearlyCost").permitAll()
                        .requestMatchers("/updateSubscription").permitAll()
                        .requestMatchers("/deleteSubscription/{id}").permitAll()
                        .requestMatchers("/updatePaymentStatus").permitAll()
                        .requestMatchers("/api/chat").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationEntryPoint, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // ✅ CORS configuration bean embedded here
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000")); // Frontend origin
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}