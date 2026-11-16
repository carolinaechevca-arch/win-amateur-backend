package com.winamateur.win_amateur.iam.infrastructure.out.security.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.winamateur.win_amateur.iam.domain.model.GeneratedTokens;

import io.vavr.control.Try;

class JwtTokenProviderAdapterTest {

	private static final String SIGNING_SECRET =
			"9f0fdba61b4a4c99a80f39bb7c0bc26e841de535c42fdf4a7bc39937ad412bf8";
	private static final String ISSUER = "win-amateur";
	private static final long ACCESS_TOKEN_VALIDITY_SECONDS = 900L;
	private static final long REFRESH_TOKEN_VALIDITY_SECONDS = 2592000L;
	private static final UUID ACCOUNT_ID =
			UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID SESSION_ID =
			UUID.fromString("33333333-3333-3333-3333-333333333333");

	private final JwtTokenProviderAdapter adapter = new JwtTokenProviderAdapter(
			SIGNING_SECRET,
			ISSUER,
			ACCESS_TOKEN_VALIDITY_SECONDS,
			REFRESH_TOKEN_VALIDITY_SECONDS);

	@Test
	void shouldGenerateSignedAccessTokenAndHashedRefreshToken() {
		LocalDateTime issuedAt = LocalDateTime.now();

		GeneratedTokens tokens = adapter.generate(ACCOUNT_ID, SESSION_ID, issuedAt);

		assertThat(tokens.accessToken().split("\\.")).hasSize(3);
		assertThat(tokens.tokenType()).isEqualTo("Bearer");
		assertThat(tokens.expiresIn()).isEqualTo(ACCESS_TOKEN_VALIDITY_SECONDS);
		assertThat(tokens.sessionExpiresAt())
				.isEqualTo(issuedAt.plusSeconds(REFRESH_TOKEN_VALIDITY_SECONDS));
		assertThat(tokens.refreshTokenHash()).hasSize(64);
		assertThat(tokens.refreshTokenHash()).isEqualTo(sha256(tokens.refreshToken()));

		String payload = new String(
				Base64.getUrlDecoder().decode(tokens.accessToken().split("\\.")[1]),
				StandardCharsets.UTF_8);
		assertThat(payload).contains(ACCOUNT_ID.toString(), SESSION_ID.toString(), ISSUER);
		assertThat(adapter.validate(tokens.accessToken()))
				.hasValueSatisfying(authenticatedToken -> {
					assertThat(authenticatedToken.userAccountId()).isEqualTo(ACCOUNT_ID);
					assertThat(authenticatedToken.sessionId()).isEqualTo(SESSION_ID);
				});
		assertThat(adapter.validate(tokens.accessToken() + "invalid")).isEmpty();

		GeneratedTokens secondTokens = adapter.generate(ACCOUNT_ID, SESSION_ID, issuedAt);
		assertThat(secondTokens.refreshToken()).isNotEqualTo(tokens.refreshToken());
	}

	private String sha256(String value) {
		return Try.of(() -> MessageDigest.getInstance("SHA-256"))
				.map(digest -> digest.digest(value.getBytes(StandardCharsets.UTF_8)))
				.map(hash -> HexFormat.of().formatHex(hash))
				.get();
	}
}
