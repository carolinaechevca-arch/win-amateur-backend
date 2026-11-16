package com.winamateur.win_amateur.iam.domain.command;

import lombok.Builder;

@Builder
public record LoginCommand(
		String email,
		String password,
		String deviceId,
		String userAgent) {
}
