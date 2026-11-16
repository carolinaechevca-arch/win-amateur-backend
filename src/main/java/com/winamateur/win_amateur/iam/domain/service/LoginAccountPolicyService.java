package com.winamateur.win_amateur.iam.domain.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.enums.IdentityStatus;
import com.winamateur.win_amateur.iam.domain.exception.AccountDisabledException;
import com.winamateur.win_amateur.iam.domain.exception.AccountLockedException;
import com.winamateur.win_amateur.iam.domain.model.LoginAccount;

public class LoginAccountPolicyService {

	private static final Set<AccountStatus> DISABLED_ACCOUNT_STATUSES = Set.of(
			AccountStatus.PENDING,
			AccountStatus.DISABLED,
			AccountStatus.CLOSED);

	public void validate(LoginAccount account, LocalDateTime authenticationTime) {
		Optional.of(account.identityStatus())
				.filter(IdentityStatus.BLOCKED::equals)
				.ifPresent(status -> {
					throw new AccountDisabledException();
				});

		Optional.of(account.accountStatus())
				.filter(DISABLED_ACCOUNT_STATUSES::contains)
				.ifPresent(status -> {
					throw new AccountDisabledException();
				});

		Optional.ofNullable(account.lockedUntil())
				.filter(lockedUntil -> lockedUntil.isAfter(authenticationTime))
				.ifPresent(lockedUntil -> {
					throw new AccountLockedException();
				});

		Optional.of(account.accountStatus())
				.filter(AccountStatus.LOCKED::equals)
				.filter(status -> account.lockedUntil() == null)
				.ifPresent(status -> {
					throw new AccountLockedException();
				});
	}
}
