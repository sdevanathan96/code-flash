package com.codeflash.dto.response;

import jakarta.validation.constraints.NotBlank;

public record RenameRequest(@NotBlank String name) {}
