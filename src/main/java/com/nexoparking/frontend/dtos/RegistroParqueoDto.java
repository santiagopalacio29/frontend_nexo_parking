package com.nexoparking.frontend.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroParqueoDto {
    private Integer idRegistro;
    private String placa;
    private Integer idEspacio;
    private LocalDateTime fechaHoraEntrada;
    private LocalDateTime fechaHoraSalida;
    private Double tiempoTotal;


    public boolean isActivo() {
        return fechaHoraSalida == null;
    }
}
