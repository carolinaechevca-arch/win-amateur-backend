package com.winamateur.win_amateur.iam.infrastructure.out.security.configuration;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.HttpStatusAccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.winamateur.win_amateur.iam.application.port.out.IPasswordHasherPort;
import com.winamateur.win_amateur.iam.application.port.out.ITokenProviderPort;
import com.winamateur.win_amateur.iam.infrastructure.out.security.adapter.BCryptPasswordHasherAdapter;
import com.winamateur.win_amateur.iam.infrastructure.out.security.adapter.JwtTokenProviderAdapter;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

	private static final String BCRYPT_STRENGTH_PROPERTY =
			"${win-amateur.iam.password.bcrypt-strength}";
	private static final String ACCESS_TOKEN_VALIDITY_SECONDS_PROPERTY =
			"${win-amateur.iam.login.access-token-validity-seconds}";
	private static final String REFRESH_TOKEN_VALIDITY_SECONDS_PROPERTY =
			"${win-amateur.iam.login.refresh-token-validity-seconds}";
	private static final String TOKEN_SIGNING_SECRET_PROPERTY =
			"${win-amateur.iam.login.token-signing-secret}";
	private static final String TOKEN_ISSUER_PROPERTY =
			"${win-amateur.iam.login.token-issuer}";
	private static final String CORS_ALLOWED_ORIGINS_PROPERTY =
			"${win-amateur.cors.allowed-origin-patterns}";
	private static final String ACCOUNT_REGISTRATION_PATH = "/api/v1/accounts";
	private static final String LOGIN_PATH = "/api/v1/auth/login";
	private static final String ACTUATOR_HEALTH_PATH = "/actuator/health";
	private static final String API_PATH_PATTERN = "/api/**";
	private static final long CORS_MAX_AGE_SECONDS = 3600L;

	@Bean
	PasswordEncoder passwordEncoder(
			@Value(BCRYPT_STRENGTH_PROPERTY) int bcryptStrength) {
		return new BCryptPasswordEncoder(bcryptStrength);
	}

	@Bean
	IPasswordHasherPort passwordHasherPort(PasswordEncoder passwordEncoder) {
		return new BCryptPasswordHasherAdapter(passwordEncoder);
	}

	@Bean
	ITokenProviderPort tokenProviderPort(
			@Value(TOKEN_SIGNING_SECRET_PROPERTY) String signingSecret,
			@Value(TOKEN_ISSUER_PROPERTY) String issuer,
			@Value(ACCESS_TOKEN_VALIDITY_SECONDS_PROPERTY) long accessTokenValiditySeconds,
			@Value(REFRESH_TOKEN_VALIDITY_SECONDS_PROPERTY) long refreshTokenValiditySeconds) {
		return new JwtTokenProviderAdapter(
				signingSecret,
				issuer,
				accessTokenValiditySeconds,
				refreshTokenValiditySeconds);
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.cors(withDefaults())
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.POST, ACCOUNT_REGISTRATION_PATH).permitAll()
						.requestMatchers(HttpMethod.POST, LOGIN_PATH).permitAll()
						.requestMatchers(HttpMethod.GET, ACTUATOR_HEALTH_PATH).permitAll()
						.anyRequest().authenticated())
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
						.accessDeniedHandler(new HttpStatusAccessDeniedHandler(HttpStatus.FORBIDDEN)))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value(CORS_ALLOWED_ORIGINS_PROPERTY) List<String> allowedOriginPatterns) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(allowedOriginPatterns);
		configuration.setAllowedMethods(List.of(
				HttpMethod.GET.name(),
				HttpMethod.POST.name(),
				HttpMethod.OPTIONS.name()));
		configuration.setAllowedHeaders(List.of(
				HttpHeaders.ACCEPT,
				HttpHeaders.AUTHORIZATION,
				HttpHeaders.CONTENT_TYPE));
		configuration.setMaxAge(CORS_MAX_AGE_SECONDS);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration(API_PATH_PATTERN, configuration);
		return source;
	}
}
