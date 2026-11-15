package com.winamateur.win_amateur.iam.application.port.in;

import com.winamateur.win_amateur.iam.domain.command.RegisterAccountCommand;
import com.winamateur.win_amateur.iam.domain.result.RegistrationResult;

public interface IRegisterAccountServicePort {

	RegistrationResult register(RegisterAccountCommand command);
}
