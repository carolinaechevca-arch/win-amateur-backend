package com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.UserCredentialEntity;

public interface UserCredentialJpaRepository extends JpaRepository<UserCredentialEntity, UUID> {
}
