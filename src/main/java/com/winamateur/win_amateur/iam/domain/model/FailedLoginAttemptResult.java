package com.winamateur.win_amateur.iam.domain.model;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record FailedLoginAttemptResult(
		short failedAttempts,
		boolean locked,
		LocalDateTime lockedUntil) {
}
