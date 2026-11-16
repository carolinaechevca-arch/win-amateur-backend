package com.winamateur.win_amateur.iam.infrastructure.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity.SessionEntity;

public interface SessionJpaRepository extends JpaRepository<SessionEntity, UUID> {
}
