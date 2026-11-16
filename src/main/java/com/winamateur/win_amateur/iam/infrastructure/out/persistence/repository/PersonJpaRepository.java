package com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.winamateur.win_amateur.iam.domain.enums.IdentityStatus;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.PersonEntity;

public interface PersonJpaRepository extends JpaRepository<PersonEntity, UUID> {

	Optional<PersonEntity> findByEmailIgnoreCase(String email);

	Optional<PersonEntity> findByEmailIgnoreCaseAndIdentityStatus(String email, IdentityStatus identityStatus);

	boolean existsByDocumentCountryAndDocumentTypeAndDocumentNumberAndIdentityStatus(
			String documentCountry,
			String documentType,
			String documentNumber,
			IdentityStatus identityStatus);
}
