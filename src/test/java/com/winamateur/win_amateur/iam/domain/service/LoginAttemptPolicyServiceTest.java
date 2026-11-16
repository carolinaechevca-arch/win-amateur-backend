package com.winamateur.win_amateur.iam.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.winamateur.win_amateur.iam.domain.model.FailedLoginAttemptResult;

class LoginAttemptPolicyServiceTest {

	private static final int MAXIMUM_ATTEMPTS = 5;
	private static final long LOCK_DURATION_SECONDS = 900L;

	private final LoginAttemptPolicyService service = new LoginAttemptPolicyService();

	@Test
	void shouldIncrementAndLockFailedAttempts() {
		LocalDateTime now = LocalDateTime.now();

		FailedLoginAttemptResult regularAttempt = service.evaluate(
				(short) 2,
				null,
				now,
				MAXIMUM_ATTEMPTS,
				LOCK_DURATION_SECONDS);
		FailedLoginAttemptResult lockedAttempt = service.evaluate(
				(short) 4,
				null,
				now,
				MAXIMUM_ATTEMPTS,
				LOCK_DURATION_SECONDS);

		assertThat(regularAttempt.failedAttempts()).isEqualTo((short) 3);
		assertThat(regularAttempt.locked()).isFalse();
		assertThat(lockedAttempt.locked()).isTrue();
		assertThat(lockedAttempt.lockedUntil()).isEqualTo(now.plusSeconds(LOCK_DURATION_SECONDS));
	}

	@Test
	void shouldResetAttemptsAfterExpiredLock() {
		LocalDateTime now = LocalDateTime.now();

		FailedLoginAttemptResult result = service.evaluate(
				(short) 5,
				now.minusMinutes(1),
				now,
				MAXIMUM_ATTEMPTS,
				LOCK_DURATION_SECONDS);

		assertThat(result.failedAttempts()).isEqualTo((short) 1);
		assertThat(result.locked()).isFalse();
	}
}
