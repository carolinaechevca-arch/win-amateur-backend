package com.winamateur.win_amateur.iam.infrastructure.in.http.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winamateur.win_amateur.iam.application.port.in.ILoginServicePort;
import com.winamateur.win_amateur.iam.domain.command.LoginCommand;
import com.winamateur.win_amateur.iam.domain.result.LoginResult;
import com.winamateur.win_amateur.iam.infrastructure.in.http.dto.request.LoginRequest;
import com.winamateur.win_amateur.iam.infrastructure.in.http.dto.response.LoginResponse;
import com.winamateur.win_amateur.iam.infrastructure.in.http.mapper.LoginMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LoginController {

	private static final String USER_AGENT_HEADER = "User-Agent";

	private final ILoginServicePort loginServicePort;
	private final LoginMapper loginMapper;

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest) {

		return ResponseEntity.ok(
				loginMapper.toResponse(
						loginServicePort.login(
								loginMapper.toCommand(
										request,
										httpRequest.getHeader(USER_AGENT_HEADER)
								)
						)
				)
		);
	}
}
