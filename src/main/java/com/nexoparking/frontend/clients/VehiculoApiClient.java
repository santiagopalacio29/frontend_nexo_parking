package com.nexoparking.frontend.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nexoparking.frontend.dtos.VehiculoDto;


@FeignClient(name = "vehiculo-api", url = "${nexo.api.base-url}", path = "/api/vehiculo")
public interface VehiculoApiClient {

    @GetMapping
    List<VehiculoDto> listar();

    @GetMapping("/{placa}")
    VehiculoDto obtener(@PathVariable("placa") String placa);

    @PostMapping
    VehiculoDto crear(@RequestBody VehiculoDto vehiculo);

    @PutMapping("/{placa}")
    VehiculoDto actualizar(@PathVariable("placa") String placa, @RequestBody VehiculoDto vehiculo);

    @DeleteMapping("/{placa}")
    void eliminar(@PathVariable("placa") String placa);
}
