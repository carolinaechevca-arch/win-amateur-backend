package com.winamateur.win_amateur.iam.infrastructure.in.http.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record AccountRegistrationResponse(
		UUID personId,
		UUID userAccountId,
		String email,
		String status,
		LocalDateTime createdAt) {
}
