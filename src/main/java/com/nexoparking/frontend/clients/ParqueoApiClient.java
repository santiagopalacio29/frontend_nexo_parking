package com.nexoparking.frontend.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nexoparking.frontend.dtos.EntradaRequest;
import com.nexoparking.frontend.dtos.FacturaDto;
import com.nexoparking.frontend.dtos.RegistroParqueoDto;
import com.nexoparking.frontend.dtos.SalidaRequest;


@FeignClient(name = "parqueo-api", url = "${nexo.api.base-url}", path = "/api/parqueo")
public interface ParqueoApiClient {

    @GetMapping
    List<RegistroParqueoDto> listar();

    @GetMapping("/{id}")
    RegistroParqueoDto obtener(@PathVariable("id") Integer id);

    @PostMapping("/entrada")
    RegistroParqueoDto registrarEntrada(@RequestBody EntradaRequest entrada);

    /** El backend calcula las horas, cobra según la tarifa, genera la factura y libera el espacio. */
    @PutMapping("/{id}/salida")
    FacturaDto registrarSalida(@PathVariable("id") Integer idRegistro, @RequestBody SalidaRequest salida);
}
