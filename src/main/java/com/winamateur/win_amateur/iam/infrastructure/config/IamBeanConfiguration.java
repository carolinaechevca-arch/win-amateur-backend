package com.winamateur.win_amateur.iam.infrastructure.config;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

import com.winamateur.win_amateur.iam.application.port.in.IRegisterAccountServicePort;
import com.winamateur.win_amateur.iam.application.port.out.IAccountRegistrationPersistencePort;
import com.winamateur.win_amateur.iam.application.port.out.IPasswordHasherPort;
import com.winamateur.win_amateur.iam.application.usecase.RegisterAccountUseCase;
import com.winamateur.win_amateur.iam.domain.service.IdentityDocumentPolicyService;
import com.winamateur.win_amateur.iam.domain.service.PasswordPolicyService;
import com.winamateur.win_amateur.iam.infrastructure.in.http.mapper.AccountRegistrationMapper;
import com.winamateur.win_amateur.iam.infrastructure.out.security.adapter.BCryptPasswordHasherAdapter;

@Configuration(proxyBeanMethods = false)
public class IamBeanConfiguration {

	private static final String PASSWORD_MINIMUM_LENGTH_PROPERTY =
			"${win-amateur.iam.password.minimum-length}";
	private static final String PASSWORD_MAXIMUM_LENGTH_PROPERTY =
			"${win-amateur.iam.password.maximum-length}";
	private static final String BCRYPT_STRENGTH_PROPERTY =
			"${win-amateur.iam.password.bcrypt-strength}";
	private static final String SUPPORTED_DOCUMENT_TYPES_PROPERTY =
			"${win-amateur.iam.document.supported-types}";
	private static final String CORS_ALLOWED_ORIGINS_PROPERTY =
			"${win-amateur.cors.allowed-origin-patterns}";
	private static final String ACCOUNT_REGISTRATION_PATH = "/api/v1/accounts";
	private static final String ACTUATOR_HEALTH_PATH = "/actuator/health";
	private static final String API_PATH_PATTERN = "/api/**";
	private static final long CORS_MAX_AGE_SECONDS = 3600L;

	@Bean
	PasswordPolicyService passwordPolicyService(
			@Value(PASSWORD_MINIMUM_LENGTH_PROPERTY) int minimumLength,
			@Value(PASSWORD_MAXIMUM_LENGTH_PROPERTY) int maximumLength) {
		return new PasswordPolicyService(minimumLength, maximumLength);
	}

	@Bean
	IdentityDocumentPolicyService identityDocumentPolicyService(
			@Value(SUPPORTED_DOCUMENT_TYPES_PROPERTY) Set<String> supportedDocumentTypes) {
		Set<String> normalizedDocumentTypes = supportedDocumentTypes.stream()
				.map(String::strip)
				.map(value -> value.toUpperCase(Locale.ROOT))
				.collect(Collectors.toUnmodifiableSet());

		return new IdentityDocumentPolicyService(normalizedDocumentTypes);
	}

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
	IRegisterAccountServicePort registerAccountServicePort(
			IAccountRegistrationPersistencePort accountRegistrationPersistencePort,
			IPasswordHasherPort passwordHasherPort,
			IdentityDocumentPolicyService identityDocumentPolicyService,
			PasswordPolicyService passwordPolicyService) {
		return new RegisterAccountUseCase(
				accountRegistrationPersistencePort,
				passwordHasherPort,
				identityDocumentPolicyService,
				passwordPolicyService);
	}

	@Bean
	AccountRegistrationMapper accountRegistrationMapper() {
		return new AccountRegistrationMapper();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.cors(withDefaults())
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.POST, ACCOUNT_REGISTRATION_PATH).permitAll()
						.requestMatchers(ACTUATOR_HEALTH_PATH).permitAll()
						.anyRequest().denyAll());

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value(CORS_ALLOWED_ORIGINS_PROPERTY) List<String> allowedOriginPatterns) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(allowedOriginPatterns);
		configuration.setAllowedMethods(List.of(HttpMethod.POST.name(), HttpMethod.OPTIONS.name()));
		configuration.setAllowedHeaders(List.of(HttpHeaders.ACCEPT, HttpHeaders.CONTENT_TYPE));
		configuration.setMaxAge(CORS_MAX_AGE_SECONDS);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration(API_PATH_PATTERN, configuration);
		return source;
	}
}
