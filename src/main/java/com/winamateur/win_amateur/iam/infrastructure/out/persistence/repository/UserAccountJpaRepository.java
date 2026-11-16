package com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserAccountEntity;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, UUID> {

	boolean existsByPersonIdAndStatusNot(UUID personId, AccountStatus status);

	Optional<UserAccountEntity> findByPersonId(UUID personId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<UserAccountEntity> findLockedByUserAccountId(UUID userAccountId);
}
