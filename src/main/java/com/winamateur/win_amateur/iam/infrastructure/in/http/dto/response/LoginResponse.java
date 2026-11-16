package com.winamateur.win_amateur.iam.infrastructure.in.http.dto.response;

import java.util.UUID;

import lombok.Builder;

@Builder
public record LoginResponse(
		String accessToken,
		String refreshToken,
		String tokenType,
		long expiresIn,
		UUID sessionId,
		UUID personId,
		UUID userAccountId,
		String email,
		String status) {
}
