package com.winamateur.win_amateur.iam.application.port.in;

import com.winamateur.win_amateur.iam.domain.command.LoginCommand;
import com.winamateur.win_amateur.iam.domain.result.LoginResult;

public interface ILoginServicePort {

	LoginResult login(LoginCommand command);
}
