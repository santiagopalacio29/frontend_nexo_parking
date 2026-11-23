package com.nexoparking.frontend.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nexoparking.frontend.dtos.TarifaDto;


@FeignClient(name = "tarifa-api", url = "${nexo.api.base-url}", path = "/api/tarifa")
public interface TarifaApiClient {

    @GetMapping
    List<TarifaDto> listar();

    @GetMapping("/{id}")
    TarifaDto obtener(@PathVariable("id") Integer id);

    @PostMapping
    TarifaDto crear(@RequestBody TarifaDto tarifa);

    @PutMapping("/{id}")
    TarifaDto actualizar(@PathVariable("id") Integer id, @RequestBody TarifaDto tarifa);

    @DeleteMapping("/{id}")
    void eliminar(@PathVariable("id") Integer id);
}
