package com.winamateur.win_amateur.iam.domain.exception;

public class EmailAlreadyExistsException extends RuntimeException {

	public EmailAlreadyExistsException() {
		super("The supplied email is already registered");
	}
}
