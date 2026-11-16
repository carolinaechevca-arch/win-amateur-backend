package com.winamateur.win_amateur.iam.domain.model;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record GeneratedTokens(
		String accessToken,
		String refreshToken,
		String refreshTokenHash,
		String tokenType,
		long expiresIn,
		LocalDateTime sessionExpiresAt) {
}
