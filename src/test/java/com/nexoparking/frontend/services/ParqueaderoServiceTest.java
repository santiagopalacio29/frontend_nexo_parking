package com.nexoparking.frontend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class ParqueaderoServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 24, 10, 0);

    @Mock EspacioApiClient espacioClient;
    @Mock ParqueoApiClient parqueoClient;
    @Mock VehiculoApiClient vehiculoClient;
    @Mock FacturaApiClient facturaClient;

    private ParqueaderoService service;

    private final EspacioDto espacioLibre = EspacioDto.builder()
            .idEspacio(1).numeroEspacio(2).nivel(1).estado(true).tipoEspacio("carro").build();
    private final EspacioDto espacioOcupado = EspacioDto.builder()
            .idEspacio(2).numeroEspacio(1).nivel(1).estado(false).tipoEspacio("moto").build();

    private final RegistroParqueoDto registroActivo = RegistroParqueoDto.builder()
            .idRegistro(10).placa("XYZ987").idEspacio(2).fechaHoraEntrada(AHORA.minusHours(1)).build();
    private final RegistroParqueoDto registroCerrado = RegistroParqueoDto.builder()
            .idRegistro(9).placa("ABC123").idEspacio(1)
            .fechaHoraEntrada(AHORA.minusHours(5)).fechaHoraSalida(AHORA.minusHours(3)).tiempoTotal(2.0).build();

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(AHORA.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new ParqueaderoService(espacioClient, parqueoClient, vehiculoClient, facturaClient, reloj);
    }

    @Test
    void obtenerResumen_calculaIndicadoresDelDia() {
        when(espacioClient.listar()).thenReturn(List.of(espacioLibre, espacioOcupado));
        when(parqueoClient.listar()).thenReturn(List.of(registroCerrado, registroActivo));
        when(facturaClient.listar()).thenReturn(List.of(
                FacturaDto.builder().idFactura(1).valorTotal(6000.0).fechaPago(AHORA.minusHours(3)).build(),
                FacturaDto.builder().idFactura(2).valorTotal(9999.0).fechaPago(AHORA.minusDays(1)).build()));

        ResumenParqueadero resumen = service.obtenerResumen();

        assertEquals(2, resumen.getTotalEspacios());
        assertEquals(1, resumen.getEspaciosLibres());
        assertEquals(1, resumen.getEspaciosOcupados());
        assertEquals(1, resumen.getVehiculosDentro());
        assertEquals(1, resumen.getFacturasHoy());
        assertEquals(6000.0, resumen.getIngresosHoy());
        assertEquals("XYZ987", resumen.getPlacaPorEspacio().get(2));
        assertEquals(10, resumen.getUltimosMovimientos().get(0).getIdRegistro());
    }

    @Test
    void espaciosOrdenados_ordenaPorNivelYNumero() {
        when(espacioClient.listar()).thenReturn(List.of(espacioLibre, espacioOcupado));

        List<EspacioDto> resultado = service.espaciosOrdenados();

        assertEquals(1, resultado.get(0).getNumeroEspacio());
        assertEquals(2, resultado.get(1).getNumeroEspacio());
    }

    @Test
    void espaciosLibres_soloDevuelveEspaciosDisponibles() {
        when(espacioClient.listar()).thenReturn(List.of(espacioLibre, espacioOcupado));

        List<EspacioDto> resultado = service.espaciosLibres();

        assertEquals(List.of(espacioLibre), resultado);
    }

    @Test
    void espaciosPorId_indexaPorIdentificador() {
        when(espacioClient.listar()).thenReturn(List.of(espacioLibre, espacioOcupado));

        Map<Integer, EspacioDto> resultado = service.espaciosPorId();

        assertEquals(espacioOcupado, resultado.get(2));
    }

    @Test
    void registrosActivos_excluyeLosQueYaSalieron() {
        when(parqueoClient.listar()).thenReturn(List.of(registroCerrado, registroActivo));

        assertEquals(List.of(registroActivo), service.registrosActivos());
    }

    @Test
    void historial_soloIncluyeRegistrosConSalida() {
        when(parqueoClient.listar()).thenReturn(List.of(registroCerrado, registroActivo));

        assertEquals(List.of(registroCerrado), service.historial());
    }

    @Test
    void vehiculosFueraDelParqueadero_excluyeLosQueEstanAdentro() {
        when(parqueoClient.listar()).thenReturn(List.of(registroActivo));
        when(vehiculoClient.listar()).thenReturn(List.of(
                VehiculoDto.builder().placa("XYZ987").build(),
                VehiculoDto.builder().placa("ABC123").build()));

        List<VehiculoDto> resultado = service.vehiculosFueraDelParqueadero();

        assertEquals(1, resultado.size());
        assertEquals("ABC123", resultado.get(0).getPlaca());
    }

    @Test
    void registrarEntrada_limpiaEspaciosDeLaPlaca() {
        EntradaRequest entrada = new EntradaRequest("  ABC123 ", 1);
        when(parqueoClient.registrarEntrada(entrada)).thenReturn(registroActivo);

        service.registrarEntrada(entrada);

        assertEquals("ABC123", entrada.getPlaca());
        verify(parqueoClient).registrarEntrada(entrada);
    }

    @Test
    void registrarSalida_devuelveLaFacturaDelBackend() {
        SalidaRequest salida = new SalidaRequest("efectivo");
        FacturaDto factura = FacturaDto.builder().idFactura(5).valorTotal(3000.0).build();
        when(parqueoClient.registrarSalida(10, salida)).thenReturn(factura);

        FacturaDto resultado = service.registrarSalida(10, salida);

        assertEquals(factura, resultado);
        assertTrue(resultado.getValorTotal() > 0);
    }
}
