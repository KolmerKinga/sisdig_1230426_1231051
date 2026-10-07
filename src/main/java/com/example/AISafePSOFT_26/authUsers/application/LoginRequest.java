package com.example.AISafePSOFT_26.authUsers.application;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
