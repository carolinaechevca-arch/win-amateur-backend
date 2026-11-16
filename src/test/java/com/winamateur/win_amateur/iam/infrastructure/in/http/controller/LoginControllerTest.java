package com.winamateur.win_amateur.iam.infrastructure.in.http.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.winamateur.win_amateur.iam.application.port.in.ILoginServicePort;
import com.winamateur.win_amateur.iam.domain.command.LoginCommand;
import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.result.LoginResult;
import com.winamateur.win_amateur.iam.infrastructure.in.http.dto.request.LoginRequest;
import com.winamateur.win_amateur.iam.infrastructure.in.http.dto.response.LoginResponse;
import com.winamateur.win_amateur.iam.infrastructure.in.http.mapper.LoginMapper;

import jakarta.servlet.http.HttpServletRequest;

class LoginControllerTest {

	private static final UUID SESSION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Test
	void shouldReturnOkFromLoginControllerWithObservedRequestData() {
		ILoginServicePort servicePort = mock(ILoginServicePort.class);
		LoginMapper mapper = mock(LoginMapper.class);
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		LoginController controller = new LoginController(servicePort, mapper);
		LoginRequest request = new LoginRequest("user@example.com", "Password1!", "device");
		LoginCommand command = LoginCommand.builder().email(request.email()).build();
		LoginResult result = LoginResult.builder()
				.sessionId(SESSION_ID)
				.status(AccountStatus.ACTIVE)
				.build();
		LoginResponse response = LoginResponse.builder()
				.sessionId(SESSION_ID)
				.status("ACTIVE")
				.build();
		when(httpRequest.getHeader("User-Agent")).thenReturn("JUnit");
		when(mapper.toCommand(request, "JUnit")).thenReturn(command);
		when(servicePort.login(command)).thenReturn(result);
		when(mapper.toResponse(result)).thenReturn(response);

		ResponseEntity<LoginResponse> entity = controller.login(request, httpRequest);

		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entity.getBody()).isEqualTo(response);
		verify(mapper).toCommand(request, "JUnit");
	}
}
