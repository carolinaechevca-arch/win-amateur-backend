package com.winamateur.win_amateur.iam.infrastructure.out.security.adapter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.winamateur.win_amateur.iam.application.port.out.ITokenProviderPort;
import com.winamateur.win_amateur.iam.domain.model.AuthenticatedToken;
import com.winamateur.win_amateur.iam.domain.model.GeneratedTokens;

import io.vavr.control.Try;

public class JwtTokenProviderAdapter implements ITokenProviderPort {

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final String SESSION_ID_CLAIM = "sid";
	private static final String TOKEN_TYPE = "Bearer";
	private static final int REFRESH_TOKEN_BYTES = 32;
	private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

	private final String issuer;
	private final long accessTokenValiditySeconds;
	private final long refreshTokenValiditySeconds;
	private final SecureRandom secureRandom;
	private final JwtEncoder jwtEncoder;
	private final JwtDecoder jwtDecoder;

	public JwtTokenProviderAdapter(
			String signingSecret,
			String issuer,
			long accessTokenValiditySeconds,
			long refreshTokenValiditySeconds) {
		SecretKey secretKey = new SecretKeySpec(
				signingSecret.getBytes(StandardCharsets.UTF_8),
				HMAC_ALGORITHM);
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));

		this.issuer = issuer;
		this.accessTokenValiditySeconds = accessTokenValiditySeconds;
		this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
		this.secureRandom = new SecureRandom();
		this.jwtEncoder = NimbusJwtEncoder.withSecretKey(secretKey)
				.algorithm(MacAlgorithm.HS256)
				.build();
		this.jwtDecoder = decoder;
	}

	@Override
	public GeneratedTokens generate(UUID userAccountId, UUID sessionId, LocalDateTime issuedAt) {
		String refreshToken = generateRefreshToken();
		String refreshTokenHash = hashRefreshToken(refreshToken);
		String accessToken = generateAccessToken(userAccountId, sessionId, issuedAt);

		return buildGeneratedTokens(accessToken, refreshToken, refreshTokenHash, issuedAt);
	}

	@Override
	public Optional<AuthenticatedToken> validate(String accessToken) {
		return Try.of(() -> jwtDecoder.decode(accessToken))
				.map(this::buildAuthenticatedToken)
				.toJavaOptional();
	}

	private String generateAccessToken(UUID userAccountId, UUID sessionId, LocalDateTime issuedAt) {
		Instant issuedAtInstant = issuedAt.atZone(ZoneId.systemDefault()).toInstant();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(issuer)
				.subject(userAccountId.toString())
				.issuedAt(issuedAtInstant)
				.expiresAt(issuedAtInstant.plusSeconds(accessTokenValiditySeconds))
				.claim(SESSION_ID_CLAIM, sessionId.toString())
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
				.type("JWT")
				.build();

		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	private AuthenticatedToken buildAuthenticatedToken(Jwt jwt) {
		return AuthenticatedToken.builder()
				.userAccountId(UUID.fromString(jwt.getSubject()))
				.sessionId(UUID.fromString(jwt.getClaimAsString(SESSION_ID_CLAIM)))
				.build();
	}

	private String generateRefreshToken() {
		byte[] tokenBytes = new byte[REFRESH_TOKEN_BYTES];
		secureRandom.nextBytes(tokenBytes);
		return BASE64_URL_ENCODER.encodeToString(tokenBytes);
	}

	private String hashRefreshToken(String refreshToken) {
		return Try.of(() -> MessageDigest.getInstance("SHA-256"))
				.map(digest -> digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8)))
				.map(hash -> HexFormat.of().formatHex(hash))
				.getOrElseThrow(exception -> new IllegalStateException(
						"SHA-256 is not available",
						exception));
	}

	private GeneratedTokens buildGeneratedTokens(
			String accessToken,
			String refreshToken,
			String refreshTokenHash,
			LocalDateTime issuedAt) {
		return GeneratedTokens.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.refreshTokenHash(refreshTokenHash)
				.tokenType(TOKEN_TYPE)
				.expiresIn(accessTokenValiditySeconds)
				.sessionExpiresAt(issuedAt.plusSeconds(refreshTokenValiditySeconds))
				.build();
	}
}
