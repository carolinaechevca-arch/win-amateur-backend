package com.winamateur.win_amateur.iam.application.port.out;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.winamateur.win_amateur.iam.domain.model.AuthenticatedToken;
import com.winamateur.win_amateur.iam.domain.model.GeneratedTokens;

public interface ITokenProviderPort {

	GeneratedTokens generate(UUID userAccountId, UUID sessionId, LocalDateTime issuedAt);

	Optional<AuthenticatedToken> validate(String accessToken);
}
