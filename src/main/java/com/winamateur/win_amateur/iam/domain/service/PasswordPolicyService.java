package com.winamateur.win_amateur.iam.domain.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.winamateur.win_amateur.iam.domain.exception.PasswordPolicyViolationException;

public class PasswordPolicyService {

	private final List<PasswordRule> rules;

	public PasswordPolicyService(int minimumLength, int maximumLength) {
		this.rules = List.of(
				new PasswordRule(
						password -> password.length() >= minimumLength,
						"Password must contain at least " + minimumLength + " characters"),
				new PasswordRule(
						password -> password.length() <= maximumLength,
						"Password must contain at most " + maximumLength + " characters"),
				new PasswordRule(
						password -> password.chars().anyMatch(Character::isUpperCase),
						"Password must contain an uppercase letter"),
				new PasswordRule(
						password -> password.chars().anyMatch(Character::isLowerCase),
						"Password must contain a lowercase letter"),
				new PasswordRule(
						password -> password.chars().anyMatch(Character::isDigit),
						"Password must contain a number"),
				new PasswordRule(
						password -> password.chars().anyMatch(character -> !Character.isLetterOrDigit(character)),
						"Password must contain a special character"));
	}

	public void validate(String rawPassword) {
		String password = Optional.ofNullable(rawPassword)
				.orElseThrow(() -> new PasswordPolicyViolationException("Password is required"));

		Optional<String> violation = rules.stream()
				.filter(rule -> !rule.isSatisfiedBy(password))
				.map(PasswordRule::message)
				.findFirst();

		violation.ifPresent(message -> {
			throw new PasswordPolicyViolationException(message);
		});
	}

	private record PasswordRule(Predicate<String> predicate, String message) {

		boolean isSatisfiedBy(String password) {
			return predicate.test(password);
		}
	}
}
