package com.nexoparking.frontend.services;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nexoparking.frontend.clients.EspacioApiClient;
import com.nexoparking.frontend.clients.FacturaApiClient;
import com.nexoparking.frontend.clients.ParqueoApiClient;
import com.nexoparking.frontend.clients.VehiculoApiClient;
import com.nexoparking.frontend.dtos.EntradaRequest;
import com.nexoparking.frontend.dtos.EspacioDto;
import com.nexoparking.frontend.dtos.FacturaDto;
import com.nexoparking.frontend.dtos.RegistroParqueoDto;
import com.nexoparking.frontend.dtos.ResumenParqueadero;
import com.nexoparking.frontend.dtos.SalidaRequest;
import com.nexoparking.frontend.dtos.VehiculoDto;

@Service
public class ParqueaderoService implements ParqueaderoServiceInterface {

    private static final int MAX_ULTIMOS_MOVIMIENTOS = 5;

    private static final Comparator<EspacioDto> POR_NIVEL_Y_NUMERO = Comparator
            .comparing(EspacioDto::getNivel, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(EspacioDto::getNumeroEspacio, Comparator.nullsLast(Comparator.naturalOrder()));

    private static final Comparator<RegistroParqueoDto> MAS_RECIENTE_PRIMERO = Comparator
            .comparing(RegistroParqueoDto::getFechaHoraEntrada, Comparator.nullsLast(Comparator.reverseOrder()));

    private final EspacioApiClient espacioClient;
    private final ParqueoApiClient parqueoClient;
    private final VehiculoApiClient vehiculoClient;
    private final FacturaApiClient facturaClient;
    private final Clock clock;

    public ParqueaderoService(EspacioApiClient espacioClient,
                              ParqueoApiClient parqueoClient,
                              VehiculoApiClient vehiculoClient,
                              FacturaApiClient facturaClient,
                              Clock clock) {
        this.espacioClient = espacioClient;
        this.parqueoClient = parqueoClient;
        this.vehiculoClient = vehiculoClient;
        this.facturaClient = facturaClient;
        this.clock = clock;
    }

    @Override
    public ResumenParqueadero obtenerResumen() {
        List<EspacioDto> espacios = espaciosOrdenados();
        List<RegistroParqueoDto> registros = parqueoClient.listar();
        List<RegistroParqueoDto> activos = filtrarActivos(registros);
        List<FacturaDto> facturasHoy = facturasDelDia(facturaClient.listar());

        int libres = (int) espacios.stream().filter(EspacioDto::isDisponible).count();

        return ResumenParqueadero.builder()
                .totalEspacios(espacios.size())
                .espaciosLibres(libres)
                .espaciosOcupados(espacios.size() - libres)
                .vehiculosDentro(activos.size())
                .facturasHoy(facturasHoy.size())
                .ingresosHoy(sumarValores(facturasHoy))
                .espacios(espacios)
                .placaPorEspacio(activos.stream()
                        .filter(r -> r.getIdEspacio() != null)
                        .collect(Collectors.toMap(RegistroParqueoDto::getIdEspacio,
                                RegistroParqueoDto::getPlaca, (a, b) -> a)))
                .ultimosMovimientos(registros.stream()
                        .sorted(MAS_RECIENTE_PRIMERO)
                        .limit(MAX_ULTIMOS_MOVIMIENTOS)
                        .toList())
                .build();
    }

    @Override
    public List<EspacioDto> espaciosOrdenados() {
        return espacioClient.listar().stream().sorted(POR_NIVEL_Y_NUMERO).toList();
    }

    @Override
    public List<EspacioDto> espaciosLibres() {
        return espaciosOrdenados().stream().filter(EspacioDto::isDisponible).toList();
    }

    @Override
    public Map<Integer, EspacioDto> espaciosPorId() {
        return espacioClient.listar().stream()
                .filter(e -> e.getIdEspacio() != null)
                .collect(Collectors.toMap(EspacioDto::getIdEspacio, Function.identity(), (a, b) -> a));
    }

    @Override
    public List<RegistroParqueoDto> registrosActivos() {
        return filtrarActivos(parqueoClient.listar());
    }

    @Override
    public List<RegistroParqueoDto> historial() {
        return parqueoClient.listar().stream()
                .filter(r -> !r.isActivo())
                .sorted(MAS_RECIENTE_PRIMERO)
                .toList();
    }

    @Override
    public List<VehiculoDto> vehiculosFueraDelParqueadero() {
        Set<String> placasDentro = registrosActivos().stream()
                .map(RegistroParqueoDto::getPlaca)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        return vehiculoClient.listar().stream()
                .filter(v -> v.getPlaca() != null && !placasDentro.contains(v.getPlaca().toUpperCase()))
                .sorted(Comparator.comparing(VehiculoDto::getPlaca))
                .toList();
    }

    @Override
    public RegistroParqueoDto registrarEntrada(EntradaRequest entrada) {
        entrada.setPlaca(entrada.getPlaca().trim());
        return parqueoClient.registrarEntrada(entrada);
    }

    @Override
    public FacturaDto registrarSalida(Integer idRegistro, SalidaRequest salida) {
        return parqueoClient.registrarSalida(idRegistro, salida);
    }

    private List<RegistroParqueoDto> filtrarActivos(List<RegistroParqueoDto> registros) {
        return registros.stream()
                .filter(RegistroParqueoDto::isActivo)
                .sorted(MAS_RECIENTE_PRIMERO)
                .toList();
    }

    private List<FacturaDto> facturasDelDia(List<FacturaDto> facturas) {
        LocalDate hoy = LocalDate.now(clock);
        return facturas.stream()
                .filter(f -> f.getFechaPago() != null && f.getFechaPago().toLocalDate().equals(hoy))
                .toList();
    }

    private double sumarValores(List<FacturaDto> facturas) {
        return facturas.stream()
                .map(FacturaDto::getValorTotal)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }
}
