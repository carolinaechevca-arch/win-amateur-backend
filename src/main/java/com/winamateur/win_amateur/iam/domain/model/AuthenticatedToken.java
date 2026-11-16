package com.winamateur.win_amateur.iam.domain.model;

import java.util.UUID;

import lombok.Builder;

@Builder
public record AuthenticatedToken(
		UUID userAccountId,
		UUID sessionId) {
}
