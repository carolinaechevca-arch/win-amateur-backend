package com.winamateur.win_amateur.iam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.winamateur.win_amateur.iam.application.port.out.IAuthenticationPersistencePort;
import com.winamateur.win_amateur.iam.application.port.out.IPasswordHasherPort;
import com.winamateur.win_amateur.iam.application.port.out.ITokenProviderPort;
import com.winamateur.win_amateur.iam.domain.command.LoginCommand;
import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.enums.IdentityStatus;
import com.winamateur.win_amateur.iam.domain.exception.AccountLockedException;
import com.winamateur.win_amateur.iam.domain.exception.InvalidCredentialsException;
import com.winamateur.win_amateur.iam.domain.model.FailedLoginAttemptResult;
import com.winamateur.win_amateur.iam.domain.model.GeneratedTokens;
import com.winamateur.win_amateur.iam.domain.model.LoginAccount;
import com.winamateur.win_amateur.iam.domain.model.SuccessfulLogin;
import com.winamateur.win_amateur.iam.domain.result.LoginResult;
import com.winamateur.win_amateur.iam.domain.service.LoginAccountPolicyService;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

	private static final int MAXIMUM_FAILED_ATTEMPTS = 5;
	private static final long LOCK_DURATION_SECONDS = 900L;
	private static final String REFERENCE_PASSWORD_HASH = "reference-hash";
	private static final String RAW_PASSWORD = "Secret-Password1!";
	private static final UUID PERSON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID USER_ACCOUNT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Mock
	private IAuthenticationPersistencePort authenticationPersistencePort;

	@Mock
	private IPasswordHasherPort passwordHasherPort;

	@Mock
	private ITokenProviderPort tokenProviderPort;

	@Mock
	private LoginAccountPolicyService loginAccountPolicyService;

	private LoginUseCase loginUseCase;

	@BeforeEach
	void setUp() {
		loginUseCase = new LoginUseCase(
				authenticationPersistencePort,
				passwordHasherPort,
				tokenProviderPort,
				loginAccountPolicyService,
				MAXIMUM_FAILED_ATTEMPTS,
				LOCK_DURATION_SECONDS,
				REFERENCE_PASSWORD_HASH);
	}

	@Test
	void shouldCompleteLoginAndReturnTokens() {
		LoginAccount account = activeAccount();
		LoginCommand command = loginCommand();
		GeneratedTokens tokens = generatedTokens();
		when(authenticationPersistencePort.findByEmail(command.email())).thenReturn(Optional.of(account));
		when(passwordHasherPort.matches(RAW_PASSWORD, account.passwordHash())).thenReturn(true);
		when(tokenProviderPort.generate(any(UUID.class), any(UUID.class), any(LocalDateTime.class)))
				.thenReturn(tokens);

		LoginResult result = loginUseCase.login(command);

		assertThat(result.accessToken()).isEqualTo(tokens.accessToken());
		assertThat(result.refreshToken()).isEqualTo(tokens.refreshToken());
		assertThat(result.userAccountId()).isEqualTo(USER_ACCOUNT_ID);
		assertThat(result.personId()).isEqualTo(PERSON_ID);
		assertThat(result.status()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(result.sessionId()).isNotNull();

		ArgumentCaptor<SuccessfulLogin> captor = ArgumentCaptor.forClass(SuccessfulLogin.class);
		verify(authenticationPersistencePort).completeSuccessfulLogin(captor.capture());
		assertThat(captor.getValue().refreshTokenHash()).isEqualTo(tokens.refreshTokenHash());
		assertThat(captor.getValue().deviceId()).isEqualTo(command.deviceId());
	}

	@Test
	void shouldUseReferenceHashWhenEmailDoesNotExist() {
		LoginCommand command = loginCommand();
		when(authenticationPersistencePort.findByEmail(command.email())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> loginUseCase.login(command))
				.isInstanceOf(InvalidCredentialsException.class);

		verify(passwordHasherPort).matches(RAW_PASSWORD, REFERENCE_PASSWORD_HASH);
		verify(authenticationPersistencePort, never()).registerFailedAttempt(
				any(UUID.class),
				any(LocalDateTime.class),
				any(Integer.class),
				any(Long.class));
	}

	@Test
	void shouldRegisterFailedAttemptForInvalidPassword() {
		LoginAccount account = activeAccount();
		LoginCommand command = loginCommand();
		when(authenticationPersistencePort.findByEmail(command.email())).thenReturn(Optional.of(account));
		when(passwordHasherPort.matches(RAW_PASSWORD, account.passwordHash())).thenReturn(false);
		when(authenticationPersistencePort.registerFailedAttempt(
				any(UUID.class),
				any(LocalDateTime.class),
				any(Integer.class),
				any(Long.class)))
				.thenReturn(failedAttempt(false));

		assertThatThrownBy(() -> loginUseCase.login(command))
				.isInstanceOf(InvalidCredentialsException.class);

		verify(authenticationPersistencePort).registerFailedAttempt(
				any(UUID.class),
				any(LocalDateTime.class),
				any(Integer.class),
				any(Long.class));
		verify(tokenProviderPort, never()).generate(any(), any(), any());
	}

	@Test
	void shouldReturnLockedWhenFailedAttemptReachesLimit() {
		LoginAccount account = activeAccount();
		LoginCommand command = loginCommand();
		when(authenticationPersistencePort.findByEmail(command.email())).thenReturn(Optional.of(account));
		when(passwordHasherPort.matches(RAW_PASSWORD, account.passwordHash())).thenReturn(false);
		when(authenticationPersistencePort.registerFailedAttempt(
				any(UUID.class),
				any(LocalDateTime.class),
				any(Integer.class),
				any(Long.class)))
				.thenReturn(failedAttempt(true));

		assertThatThrownBy(() -> loginUseCase.login(command))
				.isInstanceOf(AccountLockedException.class);
	}

	private LoginAccount activeAccount() {
		return LoginAccount.builder()
				.personId(PERSON_ID)
				.userAccountId(USER_ACCOUNT_ID)
				.email("user@example.com")
				.identityStatus(IdentityStatus.ACTIVE)
				.accountStatus(AccountStatus.ACTIVE)
				.passwordHash("stored-hash")
				.failedAttempts((short) 0)
				.build();
	}

	private LoginCommand loginCommand() {
		return LoginCommand.builder()
				.email("user@example.com")
				.password(RAW_PASSWORD)
				.deviceId("device-1")
				.userAgent("JUnit")
				.build();
	}

	private GeneratedTokens generatedTokens() {
		return GeneratedTokens.builder()
				.accessToken("access-token")
				.refreshToken("refresh-token")
				.refreshTokenHash("refresh-hash")
				.tokenType("Bearer")
				.expiresIn(900L)
				.sessionExpiresAt(LocalDateTime.now().plusDays(30))
				.build();
	}

	private FailedLoginAttemptResult failedAttempt(boolean locked) {
		return FailedLoginAttemptResult.builder()
				.failedAttempts((short) (locked ? MAXIMUM_FAILED_ATTEMPTS : 1))
				.locked(locked)
				.lockedUntil(locked ? LocalDateTime.now().plusMinutes(15) : null)
				.build();
	}
}
