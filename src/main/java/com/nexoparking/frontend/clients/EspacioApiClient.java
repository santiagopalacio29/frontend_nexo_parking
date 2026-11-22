package com.nexoparking.frontend.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nexoparking.frontend.dtos.EspacioDto;

/**
 * Consume /api/espacio del backend.
 * El PUT del backend solo actualiza el campo estado (libre / no disponible).
 */
@FeignClient(name = "espacio-api", url = "${nexo.api.base-url}", path = "/api/espacio")
public interface EspacioApiClient {

    @GetMapping
    List<EspacioDto> listar();

    @PostMapping
    EspacioDto crear(@RequestBody EspacioDto espacio);

    @PutMapping("/{id}")
    EspacioDto actualizarEstado(@PathVariable("id") Integer id, @RequestBody EspacioDto espacio);
}
