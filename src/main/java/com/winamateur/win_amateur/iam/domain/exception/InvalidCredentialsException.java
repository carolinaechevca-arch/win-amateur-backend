package com.winamateur.win_amateur.iam.domain.exception;

public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("The provided credentials are invalid");
	}
}
