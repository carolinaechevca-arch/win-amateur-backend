package com.winamateur.win_amateur.iam.application.usecase;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import com.winamateur.win_amateur.iam.domain.exception.DocumentAlreadyExistsException;
import com.winamateur.win_amateur.iam.domain.exception.EmailAlreadyExistsException;
import com.winamateur.win_amateur.iam.application.port.in.IRegisterAccountServicePort;
import com.winamateur.win_amateur.iam.application.port.out.IAccountRegistrationPersistencePort;
import com.winamateur.win_amateur.iam.application.port.out.IPasswordHasherPort;
import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.enums.IdentityStatus;
import com.winamateur.win_amateur.iam.domain.model.NewAccountRegistration;
import com.winamateur.win_amateur.iam.domain.command.RegisterAccountCommand;
import com.winamateur.win_amateur.iam.domain.result.RegistrationResult;
import com.winamateur.win_amateur.iam.domain.service.IdentityDocumentPolicyService;
import com.winamateur.win_amateur.iam.domain.service.PasswordPolicyService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegisterAccountUseCase implements IRegisterAccountServicePort {

	private final IAccountRegistrationPersistencePort accountRegistrationPersistencePort;
	private final IPasswordHasherPort passwordHasherPort;
	private final IdentityDocumentPolicyService identityDocumentPolicyService;
	private final PasswordPolicyService passwordPolicyService;


	@Override
	public RegistrationResult register(RegisterAccountCommand command) {
		identityDocumentPolicyService.validate(command.documentType());
		passwordPolicyService.validate(command.password());
		validateDocumentAvailability(command);
		validateEmailAvailability(command.email());

		UUID personId = UUID.randomUUID();
		UUID userAccountId = UUID.randomUUID();
		LocalDateTime registrationTime = LocalDateTime.now();
		String passwordHash = passwordHasherPort.hash(command.password());

		NewAccountRegistration registration = buildNewAccountRegistration(
				command,
				personId,
				userAccountId,
				registrationTime,
				passwordHash);

		accountRegistrationPersistencePort.save(registration);

		return buildRegistrationResult(registration);
	}

	private NewAccountRegistration buildNewAccountRegistration(
			RegisterAccountCommand command,
			UUID personId,
			UUID userAccountId,
			LocalDateTime registrationTime,
			String passwordHash) {
		return NewAccountRegistration.builder()
				.personId(personId)
				.userAccountId(userAccountId)
				.firstName(command.firstName())
				.middleName(command.middleName())
				.firstSurname(command.firstSurname())
				.secondSurname(command.secondSurname())
				.documentCountry(command.documentCountry())
				.documentType(command.documentType())
				.documentNumber(command.documentNumber())
				.birthDate(command.birthDate())
				.email(command.email())
				.identityStatus(IdentityStatus.ACTIVE)
				.accountStatus(AccountStatus.PENDING)
				.passwordHash(passwordHash)
				.createdAt(registrationTime)
				.build();
	}

	private RegistrationResult buildRegistrationResult(NewAccountRegistration registration) {
		return RegistrationResult.builder()
				.personId(registration.personId())
				.userAccountId(registration.userAccountId())
				.email(registration.email())
				.status(registration.accountStatus())
				.createdAt(registration.createdAt())
				.build();
	}

	private void validateDocumentAvailability(RegisterAccountCommand command) {
		boolean documentAlreadyExists = accountRegistrationPersistencePort.existsActiveByDocument(
				command.documentCountry(),
				command.documentType(),
				command.documentNumber());

		rejectWhen(documentAlreadyExists, DocumentAlreadyExistsException::new);
	}

	private void validateEmailAvailability(String email) {
		boolean emailAlreadyExists = accountRegistrationPersistencePort.existsActiveByEmail(email);
		rejectWhen(emailAlreadyExists, EmailAlreadyExistsException::new);
	}

	private void rejectWhen(boolean condition, Supplier<? extends RuntimeException> exceptionSupplier) {
		Optional.of(condition)
				.filter(Boolean.TRUE::equals)
				.ifPresent(value -> {
					throw exceptionSupplier.get();
				});
	}
}
