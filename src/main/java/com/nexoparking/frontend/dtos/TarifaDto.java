package com.nexoparking.frontend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarifaDto {
    private Integer idTarifa;
    private String tipoVehiculo;
    private Double valorHora;
    private Double valorDia;
    private Double valorMensualidad;
}
