package com.winamateur.win_amateur.iam.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record SuccessfulLogin(
		UUID userAccountId,
		UUID sessionId,
		String refreshTokenHash,
		String deviceId,
		String userAgent,
		LocalDateTime expiresAt,
		LocalDateTime occurredAt) {
}
