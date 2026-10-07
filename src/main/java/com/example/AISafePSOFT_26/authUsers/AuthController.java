package com.example.AISafePSOFT_26.authUsers;

import com.example.AISafePSOFT_26.authUsers.application.AuthenticateCollaboratorUseCase;
import com.example.AISafePSOFT_26.authUsers.application.LoginRequest;
import com.example.AISafePSOFT_26.authUsers.application.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticateCollaboratorUseCase authenticateCollaborator;

    public AuthController(AuthenticateCollaboratorUseCase authenticateCollaborator) {
        this.authenticateCollaborator = authenticateCollaborator;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticateCollaborator.execute(request);
    }
}
