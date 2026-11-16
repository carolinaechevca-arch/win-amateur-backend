package com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserCredentialEntity;

public interface UserCredentialJpaRepository extends JpaRepository<UserCredentialEntity, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<UserCredentialEntity> findLockedByUserAccountId(UUID userAccountId);
}
