package com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.winamateur.win_amateur.iam.domain.model.FailedLoginAttemptResult;

@Entity
@Table(name = "user_credentials", schema = "iam")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCredentialEntity {

	@Id
	@Column(name = "user_account_id", nullable = false, updatable = false)
	private UUID userAccountId;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(name = "password_changed_at", nullable = false)
	private LocalDateTime passwordChangedAt;

	@Column(name = "failed_attempts", nullable = false)
	private short failedAttempts;

	@Column(name = "locked_until")
	private LocalDateTime lockedUntil;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public void registerFailedLogin(FailedLoginAttemptResult failedAttempt, LocalDateTime updateTime) {
		this.failedAttempts = failedAttempt.failedAttempts();
		this.lockedUntil = failedAttempt.lockedUntil();
		this.updatedAt = updateTime;
	}

	public void registerSuccessfulLogin(LocalDateTime updateTime) {
		this.failedAttempts = 0;
		this.lockedUntil = null;
		this.updatedAt = updateTime;
	}
}
