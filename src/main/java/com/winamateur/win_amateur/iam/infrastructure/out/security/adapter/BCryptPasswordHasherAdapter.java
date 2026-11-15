package com.winamateur.win_amateur.iam.infrastructure.out.security.adapter;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.winamateur.win_amateur.iam.application.port.out.IPasswordHasherPort;

public class BCryptPasswordHasherAdapter implements IPasswordHasherPort {

	private final PasswordEncoder passwordEncoder;

	public BCryptPasswordHasherAdapter(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public String hash(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}
}
