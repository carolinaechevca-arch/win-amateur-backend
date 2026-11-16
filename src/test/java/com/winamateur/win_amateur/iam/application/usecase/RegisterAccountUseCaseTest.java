package com.winamateur.win_amateur.iam.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.winamateur.win_amateur.iam.application.port.out.IAccountRegistrationPersistencePort;
import com.winamateur.win_amateur.iam.application.port.out.IPasswordHasherPort;
import com.winamateur.win_amateur.iam.domain.command.RegisterAccountCommand;
import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.domain.enums.IdentityStatus;
import com.winamateur.win_amateur.iam.domain.exception.DocumentAlreadyExistsException;
import com.winamateur.win_amateur.iam.domain.exception.EmailAlreadyExistsException;
import com.winamateur.win_amateur.iam.domain.model.NewAccountRegistration;
import com.winamateur.win_amateur.iam.domain.result.RegistrationResult;
import com.winamateur.win_amateur.iam.domain.service.IdentityDocumentPolicyService;
import com.winamateur.win_amateur.iam.domain.service.PasswordPolicyService;

@ExtendWith(MockitoExtension.class)
class RegisterAccountUseCaseTest {

	@Mock
	private IAccountRegistrationPersistencePort persistencePort;

	@Mock
	private IPasswordHasherPort passwordHasherPort;

	@Mock
	private IdentityDocumentPolicyService identityDocumentPolicyService;

	@Mock
	private PasswordPolicyService passwordPolicyService;

	private RegisterAccountUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new RegisterAccountUseCase(
				persistencePort,
				passwordHasherPort,
				identityDocumentPolicyService,
				passwordPolicyService);
	}

	@Test
	void shouldRegisterPendingAccount() {
		RegisterAccountCommand command = command();
		when(passwordHasherPort.hash(command.password())).thenReturn("password-hash");

		RegistrationResult result = useCase.register(command);

		ArgumentCaptor<NewAccountRegistration> captor = ArgumentCaptor.forClass(NewAccountRegistration.class);
		verify(persistencePort).save(captor.capture());
		NewAccountRegistration registration = captor.getValue();
		assertThat(registration.identityStatus()).isEqualTo(IdentityStatus.ACTIVE);
		assertThat(registration.accountStatus()).isEqualTo(AccountStatus.PENDING);
		assertThat(registration.passwordHash()).isEqualTo("password-hash");
		assertThat(result.personId()).isEqualTo(registration.personId());
		assertThat(result.userAccountId()).isEqualTo(registration.userAccountId());
		assertThat(result.status()).isEqualTo(AccountStatus.PENDING);
	}

	@Test
	void shouldRejectExistingDocument() {
		RegisterAccountCommand command = command();
		when(persistencePort.existsActiveByDocument("CO", "CC", "00123")).thenReturn(true);

		assertThatThrownBy(() -> useCase.register(command))
				.isInstanceOf(DocumentAlreadyExistsException.class);

		verify(persistencePort, never()).save(any());
	}

	@Test
	void shouldRejectExistingEmail() {
		RegisterAccountCommand command = command();
		when(persistencePort.existsActiveByEmail(command.email())).thenReturn(true);

		assertThatThrownBy(() -> useCase.register(command))
				.isInstanceOf(EmailAlreadyExistsException.class);

		verify(persistencePort, never()).save(any());
	}

	private RegisterAccountCommand command() {
		return RegisterAccountCommand.builder()
				.firstName("Carolina")
				.middleName(null)
				.firstSurname("Echeverri")
				.secondSurname(null)
				.documentCountry("CO")
				.documentType("CC")
				.documentNumber("00123")
				.birthDate(LocalDate.of(1990, 1, 1))
				.email("user@example.com")
				.password("Password1!")
				.build();
	}
}
