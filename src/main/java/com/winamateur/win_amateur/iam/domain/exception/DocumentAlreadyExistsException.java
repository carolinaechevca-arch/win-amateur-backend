package com.winamateur.win_amateur.iam.domain.exception;

public class DocumentAlreadyExistsException extends RuntimeException {

	public DocumentAlreadyExistsException() {
		super("An active person already exists with the supplied identity document");
	}
}
