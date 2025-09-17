package com.vibebooks.api.dto;

public record UsuarioCreateDTO(
   String nomeUsuario,
   String email,
   String senhaPura
) {}
