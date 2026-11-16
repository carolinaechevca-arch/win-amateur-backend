package com.winamateur.win_amateur.iam.domain.exception;

public class AccountLockedException extends RuntimeException {

	public AccountLockedException() {
		super("The account is temporarily locked");
	}
}
