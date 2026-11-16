package com.winamateur.win_amateur.iam.application.usecase;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.winamateur.win_amateur.iam.application.port.in.ILoginServicePort;
import com.winamateur.win_amateur.iam.application.port.out.IAuthenticationPersistencePort;
import com.winamateur.win_amateur.iam.application.port.out.IPasswordHasherPort;
import com.winamateur.win_amateur.iam.application.port.out.ITokenProviderPort;
import com.winamateur.win_amateur.iam.domain.command.LoginCommand;
import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.exception.AccountLockedException;
import com.winamateur.win_amateur.iam.domain.exception.InvalidCredentialsException;
import com.winamateur.win_amateur.iam.domain.model.FailedLoginAttemptResult;
import com.winamateur.win_amateur.iam.domain.model.GeneratedTokens;
import com.winamateur.win_amateur.iam.domain.model.LoginAccount;
import com.winamateur.win_amateur.iam.domain.model.SuccessfulLogin;
import com.winamateur.win_amateur.iam.domain.result.LoginResult;
import com.winamateur.win_amateur.iam.domain.service.LoginAccountPolicyService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoginUseCase implements ILoginServicePort {

	private final IAuthenticationPersistencePort authenticationPersistencePort;
	private final IPasswordHasherPort passwordHasherPort;
	private final ITokenProviderPort tokenProviderPort;
	private final LoginAccountPolicyService loginAccountPolicyService;
	private final int maximumFailedAttempts;
	private final long lockDurationSeconds;
	private final String referencePasswordHash;

	@Override
	public LoginResult login(LoginCommand command) {
		LocalDateTime authenticationTime = LocalDateTime.now();
		LoginAccount account = findAccount(command);

		loginAccountPolicyService.validate(account, authenticationTime);
		validatePassword(command.password(), account, authenticationTime);

		UUID sessionId = UUID.randomUUID();
		GeneratedTokens tokens = tokenProviderPort.generate(
				account.userAccountId(),
				sessionId,
				authenticationTime);
		SuccessfulLogin successfulLogin = buildSuccessfulLogin(
				command,
				account,
				sessionId,
				tokens,
				authenticationTime);

		authenticationPersistencePort.completeSuccessfulLogin(successfulLogin);

		return buildLoginResult(account, sessionId, tokens);
	}

	private LoginAccount findAccount(LoginCommand command) {
		return authenticationPersistencePort.findByEmail(command.email())
				.orElseGet(() -> rejectUnknownAccount(command.password()));
	}

	private LoginAccount rejectUnknownAccount(String rawPassword) {
		passwordHasherPort.matches(rawPassword, referencePasswordHash);
		throw new InvalidCredentialsException();
	}

	private void validatePassword(
			String rawPassword,
			LoginAccount account,
			LocalDateTime authenticationTime) {
		boolean passwordMatches = passwordHasherPort.matches(rawPassword, account.passwordHash());

		Optional.of(passwordMatches)
				.filter(Boolean.FALSE::equals)
				.ifPresent(value -> rejectInvalidPassword(account, authenticationTime));
	}

	private void rejectInvalidPassword(LoginAccount account, LocalDateTime authenticationTime) {
		FailedLoginAttemptResult failedAttempt = authenticationPersistencePort.registerFailedAttempt(
				account.userAccountId(),
				authenticationTime,
				maximumFailedAttempts,
				lockDurationSeconds);

		Optional.of(failedAttempt.locked())
				.filter(Boolean.TRUE::equals)
				.ifPresent(value -> {
					throw new AccountLockedException();
				});

		throw new InvalidCredentialsException();
	}

	private SuccessfulLogin buildSuccessfulLogin(
			LoginCommand command,
			LoginAccount account,
			UUID sessionId,
			GeneratedTokens tokens,
			LocalDateTime authenticationTime) {
		return SuccessfulLogin.builder()
				.userAccountId(account.userAccountId())
				.sessionId(sessionId)
				.refreshTokenHash(tokens.refreshTokenHash())
				.deviceId(command.deviceId())
				.userAgent(command.userAgent())
				.expiresAt(tokens.sessionExpiresAt())
				.occurredAt(authenticationTime)
				.build();
	}

	private LoginResult buildLoginResult(
			LoginAccount account,
			UUID sessionId,
			GeneratedTokens tokens) {
		return LoginResult.builder()
				.accessToken(tokens.accessToken())
				.refreshToken(tokens.refreshToken())
				.tokenType(tokens.tokenType())
				.expiresIn(tokens.expiresIn())
				.sessionId(sessionId)
				.personId(account.personId())
				.userAccountId(account.userAccountId())
				.email(account.email())
				.status(AccountStatus.ACTIVE)
				.build();
	}
}
