package com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;

@Entity
@Table(name = "user_accounts", schema = "iam")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAccountEntity {

	@Id
	@Column(name = "user_account_id", nullable = false, updatable = false)
	private UUID userAccountId;

	@Column(name = "person_id", nullable = false, updatable = false)
	private UUID personId;

	@Column(name = "status", nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private AccountStatus status;

	@Column(name = "last_login_at")
	private LocalDateTime lastLoginAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
