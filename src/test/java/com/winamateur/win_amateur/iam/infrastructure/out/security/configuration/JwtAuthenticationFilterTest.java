package com.winamateur.win_amateur.iam.infrastructure.out.security.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.winamateur.win_amateur.iam.application.port.out.ITokenProviderPort;
import com.winamateur.win_amateur.iam.domain.model.AuthenticatedToken;

import io.vavr.control.Try;

class JwtAuthenticationFilterTest {

	private static final UUID ACCOUNT_ID =
			UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID SESSION_ID =
			UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Test
	void shouldAuthenticateRequestWithValidBearerToken() {
		ITokenProviderPort tokenProviderPort = mock(ITokenProviderPort.class);
		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenProviderPort);
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();
		AuthenticatedToken authenticatedToken = AuthenticatedToken.builder()
				.userAccountId(ACCOUNT_ID)
				.sessionId(SESSION_ID)
				.build();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer access-token");
		when(tokenProviderPort.validate("access-token")).thenReturn(Optional.of(authenticatedToken));

		Try.run(() -> {
			filter.doFilter(request, response, filterChain);

			assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
			assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
					.isEqualTo(authenticatedToken);
		}).andFinally(SecurityContextHolder::clearContext)
				.get();
	}
}
