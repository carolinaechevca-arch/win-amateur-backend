package com.winamateur.win_amateur.iam.infrastructure.config.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.winamateur.win_amateur.iam.domain.exception.DocumentAlreadyExistsException;
import com.winamateur.win_amateur.iam.domain.exception.EmailAlreadyExistsException;
import com.winamateur.win_amateur.iam.domain.exception.PasswordPolicyViolationException;
import com.winamateur.win_amateur.iam.infrastructure.config.exception.response.ApiErrorResponse;

@RestControllerAdvice(basePackages = "com.winamateur.win_amateur.iam.infrastructure.in.http")
public class AccountRegistrationExceptionHandler {

	private static final String VALIDATION_ERROR = "VALIDATION_ERROR";

	@ExceptionHandler(DocumentAlreadyExistsException.class)
	public ResponseEntity<ApiErrorResponse> handleDocumentAlreadyExists(
			DocumentAlreadyExistsException exception) {
		return buildResponse(
				HttpStatus.CONFLICT,
				"DOCUMENT_ALREADY_EXISTS",
				exception.getMessage(),
				Map.of());
	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<ApiErrorResponse> handleEmailAlreadyExists(
			EmailAlreadyExistsException exception) {
		return buildResponse(
				HttpStatus.CONFLICT,
				"EMAIL_ALREADY_EXISTS",
				exception.getMessage(),
				Map.of());
	}

	@ExceptionHandler(PasswordPolicyViolationException.class)
	public ResponseEntity<ApiErrorResponse> handlePasswordPolicyViolation(
			PasswordPolicyViolationException exception) {
		return buildResponse(
				HttpStatus.UNPROCESSABLE_CONTENT,
				"PASSWORD_POLICY_VIOLATION",
				exception.getMessage(),
				Map.of("password", "Password does not comply with the configured policy"));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> fields = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.collect(Collectors.toMap(
						error -> error.getField(),
						error -> Optional.ofNullable(error.getDefaultMessage()).orElse("Invalid value"),
						(firstMessage, ignoredMessage) -> firstMessage,
						LinkedHashMap::new));

		return buildResponse(
				HttpStatus.BAD_REQUEST,
				VALIDATION_ERROR,
				"The request contains invalid fields",
				fields);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException exception) {
		return buildResponse(
				HttpStatus.BAD_REQUEST,
				VALIDATION_ERROR,
				"The request body is malformed or contains an invalid value",
				Map.of());
	}

	private ResponseEntity<ApiErrorResponse> buildResponse(
			HttpStatus status,
			String code,
			String message,
			Map<String, String> fields) {
		ApiErrorResponse response = ApiErrorResponse.builder()
				.code(code)
				.message(message)
				.fields(fields)
				.timestamp(LocalDateTime.now())
				.build();

		return ResponseEntity.status(status).body(response);
	}
}
