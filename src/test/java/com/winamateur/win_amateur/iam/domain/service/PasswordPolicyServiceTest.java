package com.winamateur.win_amateur.iam.domain.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.winamateur.win_amateur.iam.domain.exception.PasswordPolicyViolationException;

class PasswordPolicyServiceTest {

	@Test
	void shouldAcceptStrongPassword() {
		PasswordPolicyService service = new PasswordPolicyService(8, 72);

		assertThatCode(() -> service.validate("Password1!"))
				.doesNotThrowAnyException();
	}

	@Test
	void shouldRejectNullAndWeakPasswords() {
		PasswordPolicyService service = new PasswordPolicyService(8, 72);

		assertThatThrownBy(() -> service.validate(null))
				.isInstanceOf(PasswordPolicyViolationException.class);
		assertThatThrownBy(() -> service.validate("password"))
				.isInstanceOf(PasswordPolicyViolationException.class);
	}
}
