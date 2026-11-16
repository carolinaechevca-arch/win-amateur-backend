package com.winamateur.win_amateur.iam.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.enums.IdentityStatus;

import lombok.Builder;

@Builder
public record LoginAccount(
		UUID personId,
		UUID userAccountId,
		String email,
		IdentityStatus identityStatus,
		AccountStatus accountStatus,
		String passwordHash,
		short failedAttempts,
		LocalDateTime lockedUntil) {
}
