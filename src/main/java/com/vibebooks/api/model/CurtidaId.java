package com.vibebooks.api.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurtidaId implements Serializable {

    private UUID usuarioId;
    private UUID comentarioId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurtidaId curtidaId = (CurtidaId) o;
        return Objects.equals(usuarioId, curtidaId.usuarioId) &&
                Objects.equals(comentarioId, curtidaId.comentarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioId, comentarioId);
    }
}