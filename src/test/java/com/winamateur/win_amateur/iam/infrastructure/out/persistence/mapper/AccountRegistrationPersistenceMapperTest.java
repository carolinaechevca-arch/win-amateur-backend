package com.winamateur.win_amateur.iam.infrastructure.out.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.enums.IdentityStatus;
import com.winamateur.win_amateur.iam.domain.model.NewAccountRegistration;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.PersonEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserAccountEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserCredentialEntity;

class AccountRegistrationPersistenceMapperTest {

	private static final UUID PERSON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID ACCOUNT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 11, 14, 20, 0);

	private final AccountRegistrationPersistenceMapper mapper = new AccountRegistrationPersistenceMapper();

	@Test
	void shouldMapRegistrationPersistenceEntities() {
		NewAccountRegistration registration = registration();

		PersonEntity person = mapper.toPersonEntity(registration);
		UserAccountEntity account = mapper.toUserAccountEntity(registration);
		UserCredentialEntity credential = mapper.toUserCredentialEntity(registration);

		assertThat(person.getPersonId()).isEqualTo(PERSON_ID);
		assertThat(person.getIdentityStatus()).isEqualTo(IdentityStatus.ACTIVE);
		assertThat(account.getStatus()).isEqualTo(AccountStatus.PENDING);
		assertThat(credential.getPasswordHash()).isEqualTo("password-hash");
		assertThat(credential.getFailedAttempts()).isZero();
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
