package com.winamateur.win_amateur.iam.domain.command;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record RegisterAccountCommand(
        String firstName,
        String middleName,
        String firstSurname,
        String secondSurname,
        String documentCountry,
        String documentType,
        String documentNumber,
        LocalDate birthDate,
        String email,
        String password) {
}
