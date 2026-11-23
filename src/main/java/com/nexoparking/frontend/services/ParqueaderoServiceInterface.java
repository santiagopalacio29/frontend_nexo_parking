package com.nexoparking.frontend.services;

import java.util.List;
import java.util.Map;

import com.nexoparking.frontend.dtos.EspacioDto;
import com.nexoparking.frontend.dtos.FacturaDto;
import com.nexoparking.frontend.dtos.RegistroParqueoDto;
import com.nexoparking.frontend.dtos.ResumenParqueadero;
import com.nexoparking.frontend.dtos.SalidaRequest;
import com.nexoparking.frontend.dtos.EntradaRequest;
import com.nexoparking.frontend.dtos.VehiculoDto;


public interface ParqueaderoServiceInterface {

    ResumenParqueadero obtenerResumen();

    List<EspacioDto> espaciosOrdenados();

    List<EspacioDto> espaciosLibres();

    Map<Integer, EspacioDto> espaciosPorId();

    List<RegistroParqueoDto> registrosActivos();

    List<RegistroParqueoDto> historial();

    List<VehiculoDto> vehiculosFueraDelParqueadero();

    RegistroParqueoDto registrarEntrada(EntradaRequest entrada);

    FacturaDto registrarSalida(Integer idRegistro, SalidaRequest salida);
}
