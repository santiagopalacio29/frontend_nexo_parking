package com.nexoparking.frontend.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.nexoparking.frontend.dtos.FacturaDto;


@FeignClient(name = "factura-api", url = "${nexo.api.base-url}", path = "/api/factura")
public interface FacturaApiClient {

    @GetMapping
    List<FacturaDto> listar();

    @GetMapping("/{id}")
    FacturaDto obtener(@PathVariable("id") Integer id);

    @DeleteMapping("/{id}")
    void eliminar(@PathVariable("id") Integer id);
}
