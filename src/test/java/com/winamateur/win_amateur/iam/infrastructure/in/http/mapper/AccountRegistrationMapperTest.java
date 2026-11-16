package com.winamateur.win_amateur.iam.infrastructure.in.http.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.winamateur.win_amateur.iam.domain.command.RegisterAccountCommand;
import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.result.RegistrationResult;
import com.winamateur.win_amateur.iam.infrastructure.in.http.dto.request.AccountRegistrationRequest;
import com.winamateur.win_amateur.iam.infrastructure.in.http.dto.response.AccountRegistrationResponse;

class AccountRegistrationMapperTest {

	private static final UUID PERSON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID ACCOUNT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 11, 14, 20, 0);

	private final AccountRegistrationMapper mapper = new AccountRegistrationMapper();

	@Test
	void shouldMapRegistrationHttpModels() {
		AccountRegistrationRequest request = new AccountRegistrationRequest(
				" Carolina ",
				"   ",
				" Echeverri ",
				null,
				" co ",
				" cc ",
				" 00123 ",
				LocalDate.of(1990, 1, 1),
				" USER@EXAMPLE.COM ",
				"ignored-username",
				" Password 1! ");

		RegisterAccountCommand command = mapper.toCommand(request);

		assertThat(command.firstName()).isEqualTo("Carolina");
		assertThat(command.middleName()).isNull();
		assertThat(command.documentCountry()).isEqualTo("CO");
		assertThat(command.documentNumber()).isEqualTo("00123");
		assertThat(command.email()).isEqualTo("user@example.com");
		assertThat(command.password()).isEqualTo(" Password 1! ");

		RegistrationResult result = RegistrationResult.builder()
				.personId(PERSON_ID)
				.userAccountId(ACCOUNT_ID)
				.email(command.email())
				.status(AccountStatus.PENDING)
				.createdAt(CREATED_AT)
				.build();
		AccountRegistrationResponse response = mapper.toResponse(result);

		assertThat(response.status()).isEqualTo("PENDING");
		assertThat(response.personId()).isEqualTo(PERSON_ID);
	}
}
