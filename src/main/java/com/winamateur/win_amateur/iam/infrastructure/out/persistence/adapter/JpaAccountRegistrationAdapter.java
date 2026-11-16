package com.winamateur.win_amateur.iam.infrastructure.out.persistence.adapter;

import java.util.Locale;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winamateur.win_amateur.iam.domain.exception.DocumentAlreadyExistsException;
import com.winamateur.win_amateur.iam.domain.exception.EmailAlreadyExistsException;
import com.winamateur.win_amateur.iam.application.port.out.IAccountRegistrationPersistencePort;
import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.enums.IdentityStatus;
import com.winamateur.win_amateur.iam.domain.model.NewAccountRegistration;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.PersonEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserAccountEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserCredentialEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.mapper.AccountRegistrationPersistenceMapper;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository.PersonJpaRepository;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository.UserAccountJpaRepository;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository.UserCredentialJpaRepository;

import lombok.RequiredArgsConstructor;
import io.vavr.control.Try;

@Repository
@RequiredArgsConstructor
public class JpaAccountRegistrationAdapter implements IAccountRegistrationPersistencePort {

	private final PersonJpaRepository personJpaRepository;
	private final UserAccountJpaRepository userAccountJpaRepository;
	private final UserCredentialJpaRepository userCredentialJpaRepository;
	private final AccountRegistrationPersistenceMapper persistenceMapper;

	@Override
	public boolean existsActiveByDocument(
			String documentCountry,
			String documentType,
			String documentNumber) {
		return personJpaRepository.existsByDocumentCountryAndDocumentTypeAndDocumentNumberAndIdentityStatus(
				documentCountry,
				documentType,
				documentNumber,
				IdentityStatus.ACTIVE);
	}

	@Override
	public boolean existsActiveByEmail(String email) {
		return personJpaRepository.findByEmailIgnoreCaseAndIdentityStatus(email, IdentityStatus.ACTIVE)
				.map(PersonEntity::getPersonId)
				.map(personId -> userAccountJpaRepository.existsByPersonIdAndStatusNot(
						personId,
						AccountStatus.CLOSED))
				.orElse(false);
	}

	@Override
	@Transactional
	public void save(NewAccountRegistration registration) {
		PersonEntity person = persistenceMapper.toPersonEntity(registration);
		UserAccountEntity account = persistenceMapper.toUserAccountEntity(registration);
		UserCredentialEntity credential = persistenceMapper.toUserCredentialEntity(registration);

		Try.run(() -> {
			personJpaRepository.saveAndFlush(person);
			userAccountJpaRepository.saveAndFlush(account);
			userCredentialJpaRepository.saveAndFlush(credential);
		}).recoverWith(
				DataIntegrityViolationException.class,
				exception -> Try.failure(translateConstraintViolation(exception)))
				.get();
	}

	private RuntimeException translateConstraintViolation(DataIntegrityViolationException exception) {
		String databaseMessage = Optional.ofNullable(exception.getMostSpecificCause().getMessage())
				.orElse("")
				.toLowerCase(Locale.ROOT);

		return Optional.of(databaseMessage)
				.filter(message -> message.contains("email"))
				.<RuntimeException>map(message -> new EmailAlreadyExistsException())
				.orElseGet(DocumentAlreadyExistsException::new);
	}
}
