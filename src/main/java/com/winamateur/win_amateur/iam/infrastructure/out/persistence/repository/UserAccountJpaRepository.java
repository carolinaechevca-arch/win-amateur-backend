package com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.winamateur.win_amateur.iam.domain.enums.AccountStatus;
import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserAccountEntity;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, UUID> {

	boolean existsByPersonIdAndStatusNot(UUID personId, AccountStatus status);
}
