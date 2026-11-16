package com.winamateur.win_amateur.iam.domain.service;

import java.time.LocalDateTime;
import java.util.Optional;

import com.winamateur.win_amateur.iam.domain.model.FailedLoginAttemptResult;

public class LoginAttemptPolicyService {

	public FailedLoginAttemptResult evaluate(
			short currentFailedAttempts,
			LocalDateTime currentLockedUntil,
			LocalDateTime attemptedAt,
			int maximumAttempts,
			long lockDurationSeconds) {
		short effectiveFailedAttempts = Optional.ofNullable(currentLockedUntil)
				.filter(lockedUntil -> !lockedUntil.isAfter(attemptedAt))
				.map(lockedUntil -> (short) 0)
				.orElse(currentFailedAttempts);
		short nextFailedAttempts = (short) (effectiveFailedAttempts + 1);
		boolean locked = nextFailedAttempts >= maximumAttempts;
		LocalDateTime lockedUntil = Optional.of(locked)
				.filter(Boolean.TRUE::equals)
				.map(value -> attemptedAt.plusSeconds(lockDurationSeconds))
				.orElse(null);

		return buildResult(nextFailedAttempts, locked, lockedUntil);
	}

	private FailedLoginAttemptResult buildResult(
			short failedAttempts,
			boolean locked,
			LocalDateTime lockedUntil) {
		return FailedLoginAttemptResult.builder()
				.failedAttempts(failedAttempts)
				.locked(locked)
				.lockedUntil(lockedUntil)
				.build();
	}
}
