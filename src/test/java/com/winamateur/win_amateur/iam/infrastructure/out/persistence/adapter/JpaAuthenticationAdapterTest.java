package com.winamateur.win_amateur.iam.infrastructure.out.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.enums.IdentityStatus;
import com.winamateur.win_amateur.iam.domain.model.FailedLoginAttemptResult;
import com.winamateur.win_amateur.iam.domain.model.LoginAccount;
import com.winamateur.win_amateur.iam.domain.model.SuccessfulLogin;
import com.winamateur.win_amateur.iam.domain.service.LoginAttemptPolicyService;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.adapter.JpaAuthenticationAdapter;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.PersonEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.SessionEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserAccountEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserCredentialEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.mapper.AuthenticationPersistenceMapper;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository.PersonJpaRepository;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository.SessionJpaRepository;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository.UserAccountJpaRepository;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository.UserCredentialJpaRepository;

@ExtendWith(MockitoExtension.class)
class JpaAuthenticationAdapterTest {

	private static final UUID PERSON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID ACCOUNT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID SESSION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 14, 20, 0);

	@Mock
	private PersonJpaRepository personRepository;

	@Mock
	private UserAccountJpaRepository accountRepository;

	@Mock
	private UserCredentialJpaRepository credentialRepository;

	@Mock
	private SessionJpaRepository sessionRepository;

	@Mock
	private AuthenticationPersistenceMapper mapper;

	@Mock
	private LoginAttemptPolicyService attemptPolicyService;

	private JpaAuthenticationAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new JpaAuthenticationAdapter(
				personRepository,
				accountRepository,
				credentialRepository,
				sessionRepository,
				mapper,
				attemptPolicyService);
	}

	@Test
	void shouldFindCompleteLoginAccountByEmail() {
		PersonEntity person = person();
		UserAccountEntity account = account(AccountStatus.ACTIVE);
		UserCredentialEntity credential = credential((short) 0, null);
		LoginAccount expected = loginAccount();
		when(personRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(person));
		when(accountRepository.findByPersonId(PERSON_ID)).thenReturn(Optional.of(account));
		when(credentialRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(credential));
		when(mapper.toLoginAccount(person, account, credential)).thenReturn(expected);

		Optional<LoginAccount> result = adapter.findByEmail("user@example.com");

		assertThat(result).contains(expected);
	}

	@Test
	void shouldReturnEmptyWhenPersonDoesNotExist() {
		when(personRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

		assertThat(adapter.findByEmail("missing@example.com")).isEmpty();
	}

	@Test
	void shouldAtomicallyRegisterFailedAttemptAndLockAccount() {
		UserAccountEntity account = account(AccountStatus.ACTIVE);
		UserCredentialEntity credential = credential((short) 4, null);
		FailedLoginAttemptResult failedAttempt = FailedLoginAttemptResult.builder()
				.failedAttempts((short) 5)
				.locked(true)
				.lockedUntil(NOW.plusMinutes(15))
				.build();
		when(accountRepository.findLockedByUserAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
		when(credentialRepository.findLockedByUserAccountId(ACCOUNT_ID)).thenReturn(Optional.of(credential));
		when(attemptPolicyService.evaluate((short) 4, null, NOW, 5, 900L)).thenReturn(failedAttempt);

		FailedLoginAttemptResult result = adapter.registerFailedAttempt(ACCOUNT_ID, NOW, 5, 900L);

		assertThat(result).isEqualTo(failedAttempt);
		assertThat(account.getStatus()).isEqualTo(AccountStatus.LOCKED);
		assertThat(credential.getFailedAttempts()).isEqualTo((short) 5);
		assertThat(credential.getLockedUntil()).isEqualTo(NOW.plusMinutes(15));
		verify(accountRepository).save(account);
		verify(credentialRepository).save(credential);
	}

	@Test
	void shouldResetSecurityStateAndCreateSessionAfterSuccessfulLogin() {
		UserAccountEntity account = account(AccountStatus.LOCKED);
		UserCredentialEntity credential = credential((short) 5, NOW.minusMinutes(1));
		SuccessfulLogin successfulLogin = successfulLogin();
		SessionEntity session = SessionEntity.builder().sessionId(SESSION_ID).build();
		when(accountRepository.findLockedByUserAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
		when(credentialRepository.findLockedByUserAccountId(ACCOUNT_ID)).thenReturn(Optional.of(credential));
		when(mapper.toSessionEntity(successfulLogin)).thenReturn(session);

		adapter.completeSuccessfulLogin(successfulLogin);

		assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(account.getLastLoginAt()).isEqualTo(NOW);
		assertThat(credential.getFailedAttempts()).isZero();
		assertThat(credential.getLockedUntil()).isNull();
		verify(sessionRepository).saveAndFlush(session);
	}

	private PersonEntity person() {
		return PersonEntity.builder()
				.personId(PERSON_ID)
				.email("user@example.com")
				.identityStatus(IdentityStatus.ACTIVE)
				.build();
	}

	private UserAccountEntity account(AccountStatus status) {
		return UserAccountEntity.builder()
				.userAccountId(ACCOUNT_ID)
				.personId(PERSON_ID)
				.status(status)
				.build();
	}

	private UserCredentialEntity credential(short attempts, LocalDateTime lockedUntil) {
		return UserCredentialEntity.builder()
				.userAccountId(ACCOUNT_ID)
				.passwordHash("password-hash")
				.failedAttempts(attempts)
				.lockedUntil(lockedUntil)
				.build();
	}

	private LoginAccount loginAccount() {
		return LoginAccount.builder()
				.personId(PERSON_ID)
				.userAccountId(ACCOUNT_ID)
				.email("user@example.com")
				.identityStatus(IdentityStatus.ACTIVE)
				.accountStatus(AccountStatus.ACTIVE)
				.passwordHash("password-hash")
				.build();
	}

	private SuccessfulLogin successfulLogin() {
		return SuccessfulLogin.builder()
				.userAccountId(ACCOUNT_ID)
				.sessionId(SESSION_ID)
				.refreshTokenHash("refresh-hash")
				.expiresAt(NOW.plusDays(30))
				.occurredAt(NOW)
				.build();
	}
}
