package com.winamateur.win_amateur.iam.domain.exception;

public class UnsupportedDocumentTypeException extends RuntimeException {

	public UnsupportedDocumentTypeException(String documentType) {
		super("Unsupported identity document type: " + documentType);
	}
}
