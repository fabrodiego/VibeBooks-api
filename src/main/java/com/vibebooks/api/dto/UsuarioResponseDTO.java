package com.vibebooks.api.dto;

import java.util.UUID;

public record UsuarioResponseDTO(
        UUID id,
        String nomeUsuario,
        String email
) {}
