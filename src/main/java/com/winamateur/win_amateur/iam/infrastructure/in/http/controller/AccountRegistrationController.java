package com.winamateur.win_amateur.iam.infrastructure.in.http.controller;

import com.winamateur.win_amateur.iam.application.port.in.IRegisterAccountServicePort;
import com.winamateur.win_amateur.iam.infrastructure.in.http.dto.request.AccountRegistrationRequest;
import com.winamateur.win_amateur.iam.infrastructure.in.http.dto.response.AccountRegistrationResponse;
import com.winamateur.win_amateur.iam.infrastructure.in.http.mapper.AccountRegistrationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountRegistrationController {

    private final IRegisterAccountServicePort registerAccountServicePort;
    private final AccountRegistrationMapper accountRegistrationWebMapper;

    @PostMapping
    public ResponseEntity<AccountRegistrationResponse> register(
            @Valid @RequestBody AccountRegistrationRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                accountRegistrationWebMapper.toResponse(
                        registerAccountServicePort.register(
                                accountRegistrationWebMapper.toCommand(request))));
    }
}
