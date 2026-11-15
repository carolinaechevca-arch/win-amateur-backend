package com.winamateur.win_amateur.iam.infrastructure.out.persistence.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.winamateur.win_amateur.iam.domain.enums.IdentityStatus;

@Entity
@Table(name = "persons", schema = "iam")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PersonEntity {

	@Id
	@Column(name = "person_id", nullable = false, updatable = false)
	private UUID personId;

	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@Column(name = "middle_name", length = 100)
	private String middleName;

	@Column(name = "first_surname", nullable = false, length = 100)
	private String firstSurname;

	@Column(name = "second_surname", length = 100)
	private String secondSurname;

	@Column(name = "document_country", nullable = false, length = 2)
	private String documentCountry;

	@Column(name = "document_type", nullable = false, length = 20)
	private String documentType;

	@Column(name = "document_number", nullable = false, length = 60)
	private String documentNumber;

	@Column(name = "birth_date")
	private LocalDate birthDate;

	@Column(name = "email", length = 254)
	private String email;

	@Column(name = "email_verified_at")
	private LocalDateTime emailVerifiedAt;

	@Column(name = "phone", length = 20)
	private String phone;

	@Column(name = "identity_status", nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private IdentityStatus identityStatus;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
