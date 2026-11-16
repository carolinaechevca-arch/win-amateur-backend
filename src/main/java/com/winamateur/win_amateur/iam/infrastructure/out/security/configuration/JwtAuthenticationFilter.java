package com.winamateur.win_amateur.iam.infrastructure.out.security.configuration;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.winamateur.win_amateur.iam.application.port.out.ITokenProviderPort;
import com.winamateur.win_amateur.iam.domain.model.AuthenticatedToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final ITokenProviderPort tokenProviderPort;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
				.filter(header -> header.startsWith(BEARER_PREFIX))
				.map(header -> header.substring(BEARER_PREFIX.length()))
				.filter(token -> !token.isBlank())
				.flatMap(tokenProviderPort::validate)
				.filter(token -> SecurityContextHolder.getContext().getAuthentication() == null)
				.ifPresent(token -> authenticate(request, token));

		filterChain.doFilter(request, response);
	}

	private void authenticate(HttpServletRequest request, AuthenticatedToken token) {
		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(token, null, List.of());
		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

		SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(authentication);
		SecurityContextHolder.setContext(securityContext);
	}
}
