package com.winamateur.win_amateur.iam.infrastructure.out.persistence.adapter;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winamateur.win_amateur.iam.application.port.out.IAuthenticationPersistencePort;
import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.exception.InvalidCredentialsException;
import com.winamateur.win_amateur.iam.domain.model.FailedLoginAttemptResult;
import com.winamateur.win_amateur.iam.domain.model.LoginAccount;
import com.winamateur.win_amateur.iam.domain.model.SuccessfulLogin;
import com.winamateur.win_amateur.iam.domain.service.LoginAttemptPolicyService;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.PersonEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.SessionEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserAccountEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserCredentialEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.mapper.AuthenticationPersistenceMapper;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository.PersonJpaRepository;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository.SessionJpaRepository;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository.UserAccountJpaRepository;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository.UserCredentialJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaAuthenticationAdapter implements IAuthenticationPersistencePort {

	private final PersonJpaRepository personJpaRepository;
	private final UserAccountJpaRepository userAccountJpaRepository;
	private final UserCredentialJpaRepository userCredentialJpaRepository;
	private final SessionJpaRepository sessionJpaRepository;
	private final AuthenticationPersistenceMapper persistenceMapper;
	private final LoginAttemptPolicyService loginAttemptPolicyService;

	@Override
	@Transactional(readOnly = true)
	public Optional<LoginAccount> findByEmail(String email) {
		return personJpaRepository.findByEmailIgnoreCase(email)
				.flatMap(this::findAccountAndCredential);
	}

	@Override
	@Transactional
	public FailedLoginAttemptResult registerFailedAttempt(
			UUID userAccountId,
			LocalDateTime attemptedAt,
			int maximumAttempts,
			long lockDurationSeconds) {
		UserAccountEntity account = findLockedAccount(userAccountId);
		UserCredentialEntity credential = findLockedCredential(userAccountId);
		FailedLoginAttemptResult failedAttempt = loginAttemptPolicyService.evaluate(
				credential.getFailedAttempts(),
				credential.getLockedUntil(),
				attemptedAt,
				maximumAttempts,
				lockDurationSeconds);
		AccountStatus resultingStatus = Optional.of(failedAttempt.locked())
				.filter(Boolean.TRUE::equals)
				.map(value -> AccountStatus.LOCKED)
				.orElse(AccountStatus.ACTIVE);

		credential.registerFailedLogin(failedAttempt, attemptedAt);
		account.registerFailedLogin(resultingStatus, attemptedAt);
		userCredentialJpaRepository.save(credential);
		userAccountJpaRepository.save(account);

		return failedAttempt;
	}

	@Override
	@Transactional
	public void completeSuccessfulLogin(SuccessfulLogin successfulLogin) {
		UserAccountEntity account = findLockedAccount(successfulLogin.userAccountId());
		UserCredentialEntity credential = findLockedCredential(successfulLogin.userAccountId());
		SessionEntity session = persistenceMapper.toSessionEntity(successfulLogin);

		account.registerSuccessfulLogin(successfulLogin.occurredAt());
		credential.registerSuccessfulLogin(successfulLogin.occurredAt());
		userAccountJpaRepository.save(account);
		userCredentialJpaRepository.save(credential);
		sessionJpaRepository.saveAndFlush(session);
	}

	private Optional<LoginAccount> findAccountAndCredential(PersonEntity person) {
		return userAccountJpaRepository.findByPersonId(person.getPersonId())
				.flatMap(account -> userCredentialJpaRepository.findById(account.getUserAccountId())
						.map(credential -> persistenceMapper.toLoginAccount(person, account, credential)));
	}

	private UserAccountEntity findLockedAccount(UUID userAccountId) {
		return userAccountJpaRepository.findLockedByUserAccountId(userAccountId)
				.orElseThrow(InvalidCredentialsException::new);
	}

	private UserCredentialEntity findLockedCredential(UUID userAccountId) {
		return userCredentialJpaRepository.findLockedByUserAccountId(userAccountId)
				.orElseThrow(InvalidCredentialsException::new);
	}
}
