package com.winamateur.win_amateur.iam.infrastructure.in.http.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.winamateur.win_amateur.iam.application.port.in.IRegisterAccountServicePort;
import com.winamateur.win_amateur.iam.domain.command.RegisterAccountCommand;
import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.result.RegistrationResult;
import com.winamateur.win_amateur.iam.infrastructure.in.http.dto.request.AccountRegistrationRequest;
import com.winamateur.win_amateur.iam.infrastructure.in.http.dto.response.AccountRegistrationResponse;
import com.winamateur.win_amateur.iam.infrastructure.in.http.mapper.AccountRegistrationMapper;

class AccountRegistrationControllerTest {

	private static final UUID PERSON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID ACCOUNT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Test
	void shouldReturnCreatedFromRegistrationController() {
		IRegisterAccountServicePort servicePort = mock(IRegisterAccountServicePort.class);
		AccountRegistrationMapper mapper = mock(AccountRegistrationMapper.class);
		AccountRegistrationController controller = new AccountRegistrationController(servicePort, mapper);
		AccountRegistrationRequest request = registrationRequest();
		RegisterAccountCommand command = RegisterAccountCommand.builder().email("user@example.com").build();
		RegistrationResult result = RegistrationResult.builder()
				.personId(PERSON_ID)
				.userAccountId(ACCOUNT_ID)
				.status(AccountStatus.PENDING)
				.build();
		AccountRegistrationResponse response = AccountRegistrationResponse.builder()
				.personId(PERSON_ID)
				.userAccountId(ACCOUNT_ID)
				.status("PENDING")
				.build();
		when(mapper.toCommand(request)).thenReturn(command);
		when(servicePort.register(command)).thenReturn(result);
		when(mapper.toResponse(result)).thenReturn(response);

		ResponseEntity<AccountRegistrationResponse> entity = controller.register(request);

		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(entity.getBody()).isEqualTo(response);
	}

	private AccountRegistrationRequest registrationRequest() {
		return new AccountRegistrationRequest(
				"Carolina",
				null,
				"Echeverri",
				null,
				"CO",
				"CC",
				"00123",
				LocalDate.of(1990, 1, 1),
				"user@example.com",
				"carolina",
				"Password1!");
	}
}
