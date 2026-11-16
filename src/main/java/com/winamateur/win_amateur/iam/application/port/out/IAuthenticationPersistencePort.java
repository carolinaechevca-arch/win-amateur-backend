package com.winamateur.win_amateur.iam.application.port.out;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.winamateur.win_amateur.iam.domain.model.FailedLoginAttemptResult;
import com.winamateur.win_amateur.iam.domain.model.LoginAccount;
import com.winamateur.win_amateur.iam.domain.model.SuccessfulLogin;

public interface IAuthenticationPersistencePort {

	Optional<LoginAccount> findByEmail(String email);

	FailedLoginAttemptResult registerFailedAttempt(
			UUID userAccountId,
			LocalDateTime attemptedAt,
			int maximumAttempts,
			long lockDurationSeconds);

	void completeSuccessfulLogin(SuccessfulLogin successfulLogin);
}
