package com.nexoparking.frontend.dtos;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class ResumenParqueadero {
    private final int totalEspacios;
    private final int espaciosLibres;
    private final int espaciosOcupados;
    private final int vehiculosDentro;
    private final int facturasHoy;
    private final double ingresosHoy;
    private final List<EspacioDto> espacios;
    private final Map<Integer, String> placaPorEspacio;
    private final List<RegistroParqueoDto> ultimosMovimientos;
}
