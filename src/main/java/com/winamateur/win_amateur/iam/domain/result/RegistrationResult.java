package com.winamateur.win_amateur.iam.domain.result;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;

@Builder
public record RegistrationResult(
        UUID personId,
        UUID userAccountId,
        String email,
        AccountStatus status,
        LocalDateTime createdAt) {
}

