package com.winamateur.win_amateur.iam.application.port.out;

public interface IPasswordHasherPort {

	String hash(String rawPassword);

	boolean matches(String rawPassword, String passwordHash);
}
