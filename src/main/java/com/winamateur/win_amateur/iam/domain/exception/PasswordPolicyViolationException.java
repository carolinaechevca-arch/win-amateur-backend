package com.winamateur.win_amateur.iam.domain.exception;

public class PasswordPolicyViolationException extends RuntimeException {

	public PasswordPolicyViolationException(String message) {
		super(message);
	}
}
