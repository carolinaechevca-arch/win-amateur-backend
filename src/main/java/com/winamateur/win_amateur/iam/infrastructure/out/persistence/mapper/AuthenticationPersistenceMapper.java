package com.winamateur.win_amateur.iam.infrastructure.out.persistence.mapper;

import org.springframework.stereotype.Component;

import com.winamateur.win_amateur.iam.domain.model.LoginAccount;
import com.winamateur.win_amateur.iam.domain.model.SuccessfulLogin;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.PersonEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.SessionEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserAccountEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserCredentialEntity;

@Component
public class AuthenticationPersistenceMapper {

	public LoginAccount toLoginAccount(
			PersonEntity person,
			UserAccountEntity account,
			UserCredentialEntity credential) {
		return LoginAccount.builder()
				.personId(person.getPersonId())
				.userAccountId(account.getUserAccountId())
				.email(person.getEmail())
				.identityStatus(person.getIdentityStatus())
				.accountStatus(account.getStatus())
				.passwordHash(credential.getPasswordHash())
				.failedAttempts(credential.getFailedAttempts())
				.lockedUntil(credential.getLockedUntil())
				.build();
	}

	public SessionEntity toSessionEntity(SuccessfulLogin successfulLogin) {
		return SessionEntity.builder()
				.sessionId(successfulLogin.sessionId())
				.userAccountId(successfulLogin.userAccountId())
				.refreshTokenHash(successfulLogin.refreshTokenHash())
				.deviceId(successfulLogin.deviceId())
				.userAgent(successfulLogin.userAgent())
				.expiresAt(successfulLogin.expiresAt())
				.createdAt(successfulLogin.occurredAt())
				.build();
	}
}
