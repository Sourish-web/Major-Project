package com.jwt.implementation.config;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.jwt.implementation.service.JWTAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	
	@Autowired
	private AuthenticationProvider authenticationProvider;
	
	@Autowired
	private JWTAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	
	@Bean
	public SecurityFilterChain  securityFilterChain (HttpSecurity httpSecurity ) throws Exception {
		
		return httpSecurity
				
				.csrf(csrf->csrf.disable())
				.authorizeHttpRequests(auth->auth
						.requestMatchers("/auth/**").permitAll()
						.requestMatchers("/getUsers").permitAll()
						.requestMatchers("/add").permitAll()
						.requestMatchers("/all").permitAll()
						.requestMatchers("/update").permitAll()
						.requestMatchers("/delete/{id}").permitAll()
						.requestMatchers("/addBudget").permitAll()
						.requestMatchers("/updateBudget").permitAll()
						.requestMatchers("/getBudget").permitAll()
						.requestMatchers("/Budget/delete/{id}").permitAll()
						.requestMatchers("/addAsset").permitAll()
						.requestMatchers("/getAssets").permitAll()
						.requestMatchers("/updateAsset").permitAll()
						.requestMatchers("/deleteAsset/{id}").permitAll()
						.requestMatchers("/export/pdf").permitAll()
						.requestMatchers("/export/csv").permitAll()
						.requestMatchers("/breakdown/category").permitAll()
						.requestMatchers("/breakdown/month").permitAll()
						.requestMatchers("/tax-summary").permitAll()
						.requestMatchers("/addGoal").permitAll()
						.requestMatchers("/getGoals").permitAll()
						.requestMatchers("/updateGoal").permitAll()
						.requestMatchers("/deleteGoal/{id}").permitAll()
						.requestMatchers("/addSubscription").permitAll()
						.requestMatchers("/getSubscriptions").permitAll()
						.requestMatchers("/getUpcomingRenewals").permitAll()
						.requestMatchers("/monthlyCost").permitAll()
						.requestMatchers("/yearlyCost").permitAll()
						.requestMatchers("/updateSubscription").permitAll()
						.requestMatchers("/deleteSubscription/{id}").permitAll()
						.anyRequest().authenticated()
						)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
						)
				.authenticationProvider(authenticationProvider)
				.addFilterBefore(jwtAuthenticationEntryPoint,UsernamePasswordAuthenticationFilter.class )
				.build();
				
		
	}
	
}