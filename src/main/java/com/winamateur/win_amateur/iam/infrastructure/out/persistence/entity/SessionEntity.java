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

@Entity
@Table(name = "sessions", schema = "iam")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionEntity {

	@Id
	@Column(name = "session_id", nullable = false, updatable = false)
	private UUID sessionId;

	@Column(name = "user_account_id", nullable = false, updatable = false)
	private UUID userAccountId;

	@Column(name = "refresh_token_hash", nullable = false, unique = true, length = 64)
	private String refreshTokenHash;

	@Column(name = "device_id", length = 120)
	private String deviceId;

	@Column(name = "user_agent")
	private String userAgent;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "revoked_at")
	private LocalDateTime revokedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
