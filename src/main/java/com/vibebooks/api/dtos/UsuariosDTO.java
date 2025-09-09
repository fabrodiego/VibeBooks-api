package com.vibebooks.api.dtos;


public record UsuariosDTO(
        String name,
        String email,
        String password,
        String bio) {
}
