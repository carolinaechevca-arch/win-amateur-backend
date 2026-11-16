package com.winamateur.win_amateur.iam.infrastructure.config.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.winamateur.win_amateur.iam.domain.exception.AccountDisabledException;
import com.winamateur.win_amateur.iam.domain.exception.AccountLockedException;
import com.winamateur.win_amateur.iam.domain.exception.InvalidCredentialsException;
import com.winamateur.win_amateur.iam.infrastructure.config.exception.response.ApiErrorResponse;

class AccountRegistrationExceptionHandlerTest {

	private final AccountRegistrationExceptionHandler handler = new AccountRegistrationExceptionHandler();

	@Test
	void shouldTranslateAuthenticationExceptionsToContractStatusCodes() {
		ResponseEntity<ApiErrorResponse> invalid = handler.handleInvalidCredentials(
				new InvalidCredentialsException());
		ResponseEntity<ApiErrorResponse> locked = handler.handleAccountLocked(new AccountLockedException());
		ResponseEntity<ApiErrorResponse> disabled = handler.handleAccountDisabled(new AccountDisabledException());

		assertThat(invalid.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(invalid.getBody().code()).isEqualTo("INVALID_CREDENTIALS");
		assertThat(locked.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
		assertThat(locked.getBody().code()).isEqualTo("ACCOUNT_LOCKED");
		assertThat(disabled.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(disabled.getBody().code()).isEqualTo("ACCOUNT_DISABLED");
	}
}
