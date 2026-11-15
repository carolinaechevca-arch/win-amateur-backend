package com.winamateur.win_amateur.iam.domain.service;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.winamateur.win_amateur.iam.domain.exception.UnsupportedDocumentTypeException;

public class IdentityDocumentPolicyService {

	private final Set<String> supportedDocumentTypes;

	public IdentityDocumentPolicyService(Set<String> supportedDocumentTypes) {
		this.supportedDocumentTypes = supportedDocumentTypes;
	}

	public void validate(String documentType) {
		String normalizedDocumentType = Optional.ofNullable(documentType)
				.map(String::strip)
				.map(value -> value.toUpperCase(Locale.ROOT))
				.orElse("");

		Optional.of(normalizedDocumentType)
				.filter(value -> !supportedDocumentTypes.contains(value))
				.ifPresent(value -> {
					throw new UnsupportedDocumentTypeException(value);
				});
	}
}
