package com.nexoparking.frontend.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nexoparking.frontend.dtos.ClienteDto;


@FeignClient(name = "cliente-api", url = "${nexo.api.base-url}", path = "/api/clientes")
public interface ClienteApiClient {

    @GetMapping
    List<ClienteDto> listar();

    @GetMapping("/{id}")
    ClienteDto obtener(@PathVariable("id") Integer id);

    @PostMapping
    ClienteDto crear(@RequestBody ClienteDto cliente);

    @PutMapping("/{id}")
    ClienteDto actualizar(@PathVariable("id") Integer id, @RequestBody ClienteDto cliente);

    @DeleteMapping("/{id}")
    void eliminar(@PathVariable("id") Integer id);
}
