package com.winamateur.win_amateur.iam.infrastructure.out.persistence.mapper;

import org.springframework.stereotype.Component;

import com.winamateur.win_amateur.iam.domain.model.NewAccountRegistration;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.PersonEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserAccountEntity;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserCredentialEntity;

@Component
public class AccountRegistrationPersistenceMapper {

	public PersonEntity toPersonEntity(NewAccountRegistration registration) {
		return PersonEntity.builder()
				.personId(registration.personId())
				.firstName(registration.firstName())
				.middleName(registration.middleName())
				.firstSurname(registration.firstSurname())
				.secondSurname(registration.secondSurname())
				.documentCountry(registration.documentCountry())
				.documentType(registration.documentType())
				.documentNumber(registration.documentNumber())
				.birthDate(registration.birthDate())
				.email(registration.email())
				.identityStatus(registration.identityStatus())
				.createdAt(registration.createdAt())
				.updatedAt(registration.createdAt())
				.build();
	}

	public UserAccountEntity toUserAccountEntity(NewAccountRegistration registration) {
		return UserAccountEntity.builder()
				.userAccountId(registration.userAccountId())
				.personId(registration.personId())
				.status(registration.accountStatus())
				.createdAt(registration.createdAt())
				.updatedAt(registration.createdAt())
				.build();
	}

	public UserCredentialEntity toUserCredentialEntity(NewAccountRegistration registration) {
		return UserCredentialEntity.builder()
				.userAccountId(registration.userAccountId())
				.passwordHash(registration.passwordHash())
				.passwordChangedAt(registration.createdAt())
				.failedAttempts((short) 0)
				.createdAt(registration.createdAt())
				.updatedAt(registration.createdAt())
				.build();
	}
}
