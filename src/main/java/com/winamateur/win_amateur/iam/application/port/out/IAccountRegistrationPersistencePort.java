package com.winamateur.win_amateur.iam.application.port.out;

import com.winamateur.win_amateur.iam.domain.model.NewAccountRegistration;

public interface IAccountRegistrationPersistencePort {

	boolean existsActiveByDocument(String documentCountry, String documentType, String documentNumber);

	boolean existsActiveByEmail(String email);

	void save(NewAccountRegistration registration);
}
