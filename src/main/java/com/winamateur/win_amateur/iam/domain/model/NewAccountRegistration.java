package com.winamateur.win_amateur.iam.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.enums.IdentityStatus;
import lombok.Builder;

@Builder
public record NewAccountRegistration(
		UUID personId,
		UUID userAccountId,
		String firstName,
		String middleName,
		String firstSurname,
		String secondSurname,
		String documentCountry,
		String documentType,
		String documentNumber,
		LocalDate birthDate,
		String email,
		IdentityStatus identityStatus,
		AccountStatus accountStatus,
		String passwordHash,
		LocalDateTime createdAt) {
}
