package com.winamateur.win_amateur.iam.domain.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.winamateur.win_amateur.iam.domain.exception.UnsupportedDocumentTypeException;

class IdentityDocumentPolicyServiceTest {

	@Test
	void shouldAcceptSupportedDocumentTypeIgnoringCaseAndSpaces() {
		IdentityDocumentPolicyService service = new IdentityDocumentPolicyService(Set.of("CC", "PASS"));

		assertThatCode(() -> service.validate(" cc ")).doesNotThrowAnyException();
	}

	@Test
	void shouldRejectUnsupportedDocumentType() {
		IdentityDocumentPolicyService service = new IdentityDocumentPolicyService(Set.of("CC"));

		assertThatThrownBy(() -> service.validate("TI"))
				.isInstanceOf(UnsupportedDocumentTypeException.class);
	}
}
