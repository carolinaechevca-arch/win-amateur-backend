package com.winamateur.win_amateur.iam.infrastructure.config;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.winamateur.win_amateur.iam.application.port.in.ILoginServicePort;
import com.winamateur.win_amateur.iam.application.port.in.IRegisterAccountServicePort;
import com.winamateur.win_amateur.iam.application.port.out.IAccountRegistrationPersistencePort;
import com.winamateur.win_amateur.iam.application.port.out.IAuthenticationPersistencePort;
import com.winamateur.win_amateur.iam.application.port.out.IPasswordHasherPort;
import com.winamateur.win_amateur.iam.application.port.out.ITokenProviderPort;
import com.winamateur.win_amateur.iam.application.usecase.LoginUseCase;
import com.winamateur.win_amateur.iam.application.usecase.RegisterAccountUseCase;
import com.winamateur.win_amateur.iam.domain.service.IdentityDocumentPolicyService;
import com.winamateur.win_amateur.iam.domain.service.LoginAccountPolicyService;
import com.winamateur.win_amateur.iam.domain.service.LoginAttemptPolicyService;
import com.winamateur.win_amateur.iam.domain.service.PasswordPolicyService;
import com.winamateur.win_amateur.iam.infrastructure.in.http.mapper.AccountRegistrationMapper;
import com.winamateur.win_amateur.iam.infrastructure.in.http.mapper.LoginMapper;

@Configuration(proxyBeanMethods = false)
public class IamBeanConfiguration {

	private static final String PASSWORD_MINIMUM_LENGTH_PROPERTY =
			"${win-amateur.iam.password.minimum-length}";
	private static final String PASSWORD_MAXIMUM_LENGTH_PROPERTY =
			"${win-amateur.iam.password.maximum-length}";
	private static final String SUPPORTED_DOCUMENT_TYPES_PROPERTY =
			"${win-amateur.iam.document.supported-types}";
	private static final String MAXIMUM_FAILED_ATTEMPTS_PROPERTY =
			"${win-amateur.iam.login.maximum-failed-attempts}";
	private static final String LOCK_DURATION_SECONDS_PROPERTY =
			"${win-amateur.iam.login.lock-duration-seconds}";
	private static final String REFERENCE_PASSWORD = "WinAmateur-Reference-Password-2026!";

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
	LoginAccountPolicyService loginAccountPolicyService() {
		return new LoginAccountPolicyService();
	}

	@Bean
	LoginAttemptPolicyService loginAttemptPolicyService() {
		return new LoginAttemptPolicyService();
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
	ILoginServicePort loginServicePort(
			IAuthenticationPersistencePort authenticationPersistencePort,
			IPasswordHasherPort passwordHasherPort,
			ITokenProviderPort tokenProviderPort,
			LoginAccountPolicyService loginAccountPolicyService,
			@Value(MAXIMUM_FAILED_ATTEMPTS_PROPERTY) int maximumFailedAttempts,
			@Value(LOCK_DURATION_SECONDS_PROPERTY) long lockDurationSeconds) {
		String referencePasswordHash = passwordHasherPort.hash(REFERENCE_PASSWORD);

		return new LoginUseCase(
				authenticationPersistencePort,
				passwordHasherPort,
				tokenProviderPort,
				loginAccountPolicyService,
				maximumFailedAttempts,
				lockDurationSeconds,
				referencePasswordHash);
	}

	@Bean
	AccountRegistrationMapper accountRegistrationMapper() {
		return new AccountRegistrationMapper();
	}

	@Bean
	LoginMapper loginMapper() {
		return new LoginMapper();
	}
}
