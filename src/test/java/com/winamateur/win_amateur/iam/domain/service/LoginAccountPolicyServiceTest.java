package com.winamateur.win_amateur.iam.domain.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.enums.IdentityStatus;
import com.winamateur.win_amateur.iam.domain.exception.AccountDisabledException;
import com.winamateur.win_amateur.iam.domain.exception.AccountLockedException;
import com.winamateur.win_amateur.iam.domain.model.LoginAccount;

class LoginAccountPolicyServiceTest {

	private final LoginAccountPolicyService service = new LoginAccountPolicyService();

	@Test
	void shouldAllowActiveUnlockedAccount() {
		assertThatCode(() -> service.validate(
				account(AccountStatus.ACTIVE, IdentityStatus.ACTIVE, null),
				LocalDateTime.now()))
				.doesNotThrowAnyException();
	}

	@ParameterizedTest
	@EnumSource(value = AccountStatus.class, names = {"PENDING", "DISABLED", "CLOSED"})
	void shouldRejectDisabledAccountStatuses(AccountStatus status) {
		assertThatThrownBy(() -> service.validate(
				account(status, IdentityStatus.ACTIVE, null),
				LocalDateTime.now()))
				.isInstanceOf(AccountDisabledException.class);
	}

	@Test
	void shouldRejectBlockedIdentity() {
		assertThatThrownBy(() -> service.validate(
				account(AccountStatus.ACTIVE, IdentityStatus.BLOCKED, null),
				LocalDateTime.now()))
				.isInstanceOf(AccountDisabledException.class);
	}

	@Test
	void shouldRejectActiveTemporaryLockAndAllowExpiredLock() {
		LocalDateTime now = LocalDateTime.now();

		assertThatThrownBy(() -> service.validate(
				account(AccountStatus.LOCKED, IdentityStatus.ACTIVE, now.plusMinutes(1)),
				now))
				.isInstanceOf(AccountLockedException.class);
		assertThatCode(() -> service.validate(
				account(AccountStatus.LOCKED, IdentityStatus.ACTIVE, now.minusMinutes(1)),
				now))
				.doesNotThrowAnyException();
	}

	private LoginAccount account(
			AccountStatus accountStatus,
			IdentityStatus identityStatus,
			LocalDateTime lockedUntil) {
		return LoginAccount.builder()
				.identityStatus(identityStatus)
				.accountStatus(accountStatus)
				.lockedUntil(lockedUntil)
				.build();
	}
}
