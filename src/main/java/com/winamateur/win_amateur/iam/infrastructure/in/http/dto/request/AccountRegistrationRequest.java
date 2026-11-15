package com.winamateur.win_amateur.iam.infrastructure.in.http.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AccountRegistrationRequest(
		@NotBlank @Size(max = 100) String firstName,
		@Size(max = 100) String middleName,
		@NotBlank @Size(max = 100) String firstSurname,
		@Size(max = 100) String secondSurname,
		@NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") String documentCountry,
		@NotBlank @Size(max = 20) String documentType,
		@NotBlank @Size(max = 60) String documentNumber,
		LocalDate birthDate,
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Size(max = 100) String username,
		@NotNull @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String password) {
}
