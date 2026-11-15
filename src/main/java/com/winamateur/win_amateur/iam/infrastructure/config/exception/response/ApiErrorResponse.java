package com.winamateur.win_amateur.iam.infrastructure.config.exception.response;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;

@Builder
public record ApiErrorResponse(
		String code,
		String message,
		Map<String, String> fields,
		LocalDateTime timestamp) {
}
