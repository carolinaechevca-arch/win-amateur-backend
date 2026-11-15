package com.winamateur.win_amateur.iam.infrastructure.in.http.mapper;

import java.util.Locale;
import java.util.Optional;


import com.winamateur.win_amateur.iam.domain.command.RegisterAccountCommand;
import com.winamateur.win_amateur.iam.domain.result.RegistrationResult;
import com.winamateur.win_amateur.iam.infrastructure.in.http.dto.request.AccountRegistrationRequest;
import com.winamateur.win_amateur.iam.infrastructure.in.http.dto.response.AccountRegistrationResponse;

public class AccountRegistrationMapper {

	public RegisterAccountCommand toCommand(AccountRegistrationRequest request) {
		return RegisterAccountCommand.builder()
				.firstName(normalize(request.firstName()))
				.middleName(normalizeNullable(request.middleName()))
				.firstSurname(normalize(request.firstSurname()))
				.secondSurname(normalizeNullable(request.secondSurname()))
				.documentCountry(normalize(request.documentCountry()).toUpperCase(Locale.ROOT))
				.documentType(normalize(request.documentType()).toUpperCase(Locale.ROOT))
				.documentNumber(normalize(request.documentNumber()))
				.birthDate(request.birthDate())
				.email(normalize(request.email()).toLowerCase(Locale.ROOT))
				.password(request.password())
				.build();
	}

	public AccountRegistrationResponse toResponse(RegistrationResult result) {
		return AccountRegistrationResponse.builder()
				.personId(result.personId())
				.userAccountId(result.userAccountId())
				.email(result.email())
				.status(result.status().name())
				.createdAt(result.createdAt())
				.build();
	}

	private String normalize(String value) {
		return value.strip();
	}

	private String normalizeNullable(String value) {
		return Optional.ofNullable(value)
				.filter(candidate -> !candidate.isBlank())
				.map(this::normalize)
				.orElse(null);
	}
}
