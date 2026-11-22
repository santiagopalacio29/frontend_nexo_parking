package com.nexoparking.frontend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoDto {
    private String placa;
    private String marca;
    private String color;
    private String tipoVehiculo;
    private String documentoCliente;
}
