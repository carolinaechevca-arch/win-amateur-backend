package com.winamateur.win_amateur.iam.domain.result;

import java.util.UUID;

import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;

import lombok.Builder;

@Builder
public record LoginResult(
		String accessToken,
		String refreshToken,
		String tokenType,
		long expiresIn,
		UUID sessionId,
		UUID personId,
		UUID userAccountId,
		String email,
		AccountStatus status) {
}
