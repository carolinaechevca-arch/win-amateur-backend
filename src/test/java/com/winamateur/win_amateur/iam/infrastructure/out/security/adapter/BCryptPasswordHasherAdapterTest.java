package com.winamateur.win_amateur.iam.infrastructure.out.security.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BCryptPasswordHasherAdapterTest {

	private static final String RAW_PASSWORD = "Password1!";

	@Test
	void shouldHashAndVerifyPasswordWithBcrypt() {
		BCryptPasswordHasherAdapter adapter = new BCryptPasswordHasherAdapter(
				new BCryptPasswordEncoder(4));

		String passwordHash = adapter.hash(RAW_PASSWORD);

		assertThat(passwordHash).isNotEqualTo(RAW_PASSWORD);
		assertThat(adapter.matches(RAW_PASSWORD, passwordHash)).isTrue();
		assertThat(adapter.matches("wrong-password", passwordHash)).isFalse();
	}
}
