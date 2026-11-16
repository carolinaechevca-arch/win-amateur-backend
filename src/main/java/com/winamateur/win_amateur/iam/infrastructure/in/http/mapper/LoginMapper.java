package com.winamateur.win_amateur.iam.infrastructure.in.http.mapper;

import java.util.Locale;
import java.util.Optional;

import com.winamateur.win_amateur.iam.domain.command.LoginCommand;
import com.winamateur.win_amateur.iam.domain.result.LoginResult;
import com.winamateur.win_amateur.iam.infrastructure.in.http.dto.request.LoginRequest;
import com.winamateur.win_amateur.iam.infrastructure.in.http.dto.response.LoginResponse;

public class LoginMapper {

	public LoginCommand toCommand(LoginRequest request, String userAgent) {
		return LoginCommand.builder()
				.email(request.email().strip().toLowerCase(Locale.ROOT))
				.password(request.password())
				.deviceId(normalizeNullable(request.deviceId()))
				.userAgent(userAgent)
				.build();
	}

	public LoginResponse toResponse(LoginResult result) {
		return LoginResponse.builder()
				.accessToken(result.accessToken())
				.refreshToken(result.refreshToken())
				.tokenType(result.tokenType())
				.expiresIn(result.expiresIn())
				.sessionId(result.sessionId())
				.personId(result.personId())
				.userAccountId(result.userAccountId())
				.email(result.email())
				.status(result.status().name())
				.build();
	}

	private String normalizeNullable(String value) {
		return Optional.ofNullable(value)
				.filter(candidate -> !candidate.isBlank())
				.map(String::strip)
				.orElse(null);
	}
}
