package com.winamateur.win_amateur.iam.infrastructure.in.http.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.winamateur.win_amateur.iam.domain.command.LoginCommand;
import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.result.LoginResult;
import com.winamateur.win_amateur.iam.infrastructure.in.http.dto.request.LoginRequest;
import com.winamateur.win_amateur.iam.infrastructure.in.http.dto.response.LoginResponse;

class LoginMapperTest {

	private static final UUID PERSON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID ACCOUNT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID SESSION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	private final LoginMapper mapper = new LoginMapper();

	@Test
	void shouldMapLoginWithoutChangingPassword() {
		LoginRequest request = new LoginRequest(" USER@EXAMPLE.COM ", " Password 1! ", " device-1 ");

		LoginCommand command = mapper.toCommand(request, "JUnit");

		assertThat(command.email()).isEqualTo("user@example.com");
		assertThat(command.password()).isEqualTo(" Password 1! ");
		assertThat(command.deviceId()).isEqualTo("device-1");

		LoginResult result = LoginResult.builder()
				.accessToken("access")
				.refreshToken("refresh")
				.tokenType("Bearer")
				.expiresIn(900L)
				.sessionId(SESSION_ID)
				.personId(PERSON_ID)
				.userAccountId(ACCOUNT_ID)
				.email(command.email())
				.status(AccountStatus.ACTIVE)
				.build();
		LoginResponse response = mapper.toResponse(result);

		assertThat(response.status()).isEqualTo("ACTIVE");
		assertThat(response.sessionId()).isEqualTo(SESSION_ID);
	}
}
