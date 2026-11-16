package com.winamateur.win_amateur.iam.infrastructure.out.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.enums.IdentityStatus;
import com.winamateur.win_amateur.iam.domain.model.LoginAccount;
import com.winamateur.win_amateur.iam.domain.model.NewAccountRegistration;
import com.winamateur.win_amateur.iam.domain.model.SuccessfulLogin;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.PersonEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.SessionEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserAccountEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserCredentialEntity;

class AuthenticationPersistenceMapperTest {

	private static final UUID PERSON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID ACCOUNT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID SESSION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 11, 14, 20, 0);

	private final AuthenticationPersistenceMapper mapper = new AuthenticationPersistenceMapper();
	private final AccountRegistrationPersistenceMapper registrationMapper =
			new AccountRegistrationPersistenceMapper();

	@Test
	void shouldMapAuthenticationPersistenceModels() {
		NewAccountRegistration registration = registration();
		PersonEntity person = registrationMapper.toPersonEntity(registration);
		UserAccountEntity account = registrationMapper.toUserAccountEntity(registration);
		UserCredentialEntity credential = registrationMapper.toUserCredentialEntity(registration);

		LoginAccount loginAccount = mapper.toLoginAccount(person, account, credential);

		assertThat(loginAccount.email()).isEqualTo(registration.email());
		assertThat(loginAccount.passwordHash()).isEqualTo(registration.passwordHash());

		SuccessfulLogin successfulLogin = SuccessfulLogin.builder()
				.userAccountId(ACCOUNT_ID)
				.sessionId(SESSION_ID)
				.refreshTokenHash("refresh-hash")
				.deviceId("device")
				.userAgent("JUnit")
				.expiresAt(CREATED_AT.plusDays(30))
				.occurredAt(CREATED_AT)
				.build();
		SessionEntity session = mapper.toSessionEntity(successfulLogin);

		assertThat(session.getSessionId()).isEqualTo(SESSION_ID);
		assertThat(session.getRefreshTokenHash()).isEqualTo("refresh-hash");
		assertThat(session.getRevokedAt()).isNull();
	}

	private NewAccountRegistration registration() {
		return NewAccountRegistration.builder()
				.personId(PERSON_ID)
				.userAccountId(ACCOUNT_ID)
				.firstName("Carolina")
				.firstSurname("Echeverri")
				.documentCountry("CO")
				.documentType("CC")
				.documentNumber("00123")
				.email("user@example.com")
				.identityStatus(IdentityStatus.ACTIVE)
				.accountStatus(AccountStatus.PENDING)
				.passwordHash("password-hash")
				.createdAt(CREATED_AT)
				.build();
	}
}
