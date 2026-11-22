package com.nexoparking.frontend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspacioDto {
    private Integer idEspacio;
    private Integer numeroEspacio;
    private Integer nivel;
    private Boolean estado;
    private String tipoEspacio;

    /** En el backend, estado = true significa que el espacio está libre. */
    public boolean isDisponible() {
        return Boolean.TRUE.equals(estado);
    }
}
