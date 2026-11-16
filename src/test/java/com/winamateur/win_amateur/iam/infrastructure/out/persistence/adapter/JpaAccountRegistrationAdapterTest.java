package com.winamateur.win_amateur.iam.infrastructure.out.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.springframework.dao.DataIntegrityViolationException;

import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.enums.IdentityStatus;
import com.winamateur.win_amateur.iam.domain.exception.DocumentAlreadyExistsException;
import com.winamateur.win_amateur.iam.domain.exception.EmailAlreadyExistsException;
import com.winamateur.win_amateur.iam.domain.model.NewAccountRegistration;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.adapter.JpaAccountRegistrationAdapter;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.PersonEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserAccountEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserCredentialEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.mapper.AccountRegistrationPersistenceMapper;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository.PersonJpaRepository;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository.UserAccountJpaRepository;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository.UserCredentialJpaRepository;

@ExtendWith(MockitoExtension.class)
class JpaAccountRegistrationAdapterTest {

	private static final UUID PERSON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	@Mock
	private PersonJpaRepository personRepository;

	@Mock
	private UserAccountJpaRepository accountRepository;

	@Mock
	private UserCredentialJpaRepository credentialRepository;

	@Mock
	private AccountRegistrationPersistenceMapper mapper;

	private JpaAccountRegistrationAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new JpaAccountRegistrationAdapter(
				personRepository,
				accountRepository,
				credentialRepository,
				mapper);
	}

	@Test
	void shouldCheckDocumentAndEmailWithDerivedRepositories() {
		PersonEntity person = PersonEntity.builder().personId(PERSON_ID).build();
		when(personRepository.existsByDocumentCountryAndDocumentTypeAndDocumentNumberAndIdentityStatus(
				"CO", "CC", "00123", IdentityStatus.ACTIVE)).thenReturn(true);
		when(personRepository.findByEmailIgnoreCaseAndIdentityStatus("user@example.com", IdentityStatus.ACTIVE))
				.thenReturn(Optional.of(person));
		when(accountRepository.existsByPersonIdAndStatusNot(PERSON_ID, AccountStatus.CLOSED)).thenReturn(true);

		assertThat(adapter.existsActiveByDocument("CO", "CC", "00123")).isTrue();
		assertThat(adapter.existsActiveByEmail("user@example.com")).isTrue();
	}

	@Test
	void shouldPersistAllRegistrationEntities() {
		NewAccountRegistration registration = registration();
		PersonEntity person = PersonEntity.builder().personId(PERSON_ID).build();
		UserAccountEntity account = UserAccountEntity.builder().userAccountId(registration.userAccountId()).build();
		UserCredentialEntity credential = UserCredentialEntity.builder()
				.userAccountId(registration.userAccountId())
				.build();
		when(mapper.toPersonEntity(registration)).thenReturn(person);
		when(mapper.toUserAccountEntity(registration)).thenReturn(account);
		when(mapper.toUserCredentialEntity(registration)).thenReturn(credential);

		adapter.save(registration);

		verify(personRepository).saveAndFlush(person);
		verify(accountRepository).saveAndFlush(account);
		verify(credentialRepository).saveAndFlush(credential);
	}

	@Test
	void shouldTranslateEmailConstraintViolation() {
		NewAccountRegistration registration = registration();
		PersonEntity person = PersonEntity.builder().personId(PERSON_ID).build();
		when(mapper.toPersonEntity(registration)).thenReturn(person);
		when(mapper.toUserAccountEntity(registration)).thenReturn(UserAccountEntity.builder().build());
		when(mapper.toUserCredentialEntity(registration)).thenReturn(UserCredentialEntity.builder().build());
		when(personRepository.saveAndFlush(person))
				.thenThrow(new DataIntegrityViolationException("unique email constraint"));

		assertThatThrownBy(() -> adapter.save(registration))
				.isInstanceOf(EmailAlreadyExistsException.class);
	}

	@Test
	void shouldTranslateDocumentConstraintViolation() {
		NewAccountRegistration registration = registration();
		PersonEntity person = PersonEntity.builder().personId(PERSON_ID).build();
		when(mapper.toPersonEntity(registration)).thenReturn(person);
		when(mapper.toUserAccountEntity(registration)).thenReturn(UserAccountEntity.builder().build());
		when(mapper.toUserCredentialEntity(registration)).thenReturn(UserCredentialEntity.builder().build());
		when(personRepository.saveAndFlush(person))
				.thenThrow(new DataIntegrityViolationException("unique document constraint"));

		assertThatThrownBy(() -> adapter.save(registration))
				.isInstanceOf(DocumentAlreadyExistsException.class);
	}

	private NewAccountRegistration registration() {
		return NewAccountRegistration.builder()
				.personId(PERSON_ID)
				.userAccountId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
				.email("user@example.com")
				.identityStatus(IdentityStatus.ACTIVE)
				.accountStatus(AccountStatus.PENDING)
				.passwordHash("password-hash")
				.createdAt(LocalDateTime.of(2026, 11, 14, 20, 0))
				.build();
	}
}
