package com.winamateur.win_amateur.iam.domain.exception;

public class AccountDisabledException extends RuntimeException {

	public AccountDisabledException() {
		super("The account is not enabled for login");
	}
}
