package com.winamateur.win_amateur.iam.infrastructure.in.http.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String password,
		@Size(max = 120) String deviceId) {
}
