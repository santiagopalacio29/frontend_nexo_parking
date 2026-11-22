package com.nexoparking.frontend.controllers;

import java.util.Comparator;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nexoparking.frontend.clients.ClienteApiClient;
import com.nexoparking.frontend.dtos.ClienteDto;

import feign.FeignException;

@Controller
@RequestMapping("/clientes")
public class ClienteViewController {

    private static final String VISTA_FORMULARIO = "clientes/formulario";
    private static final String REDIRECT_LISTA = "redirect:/clientes";

    private final ClienteApiClient clienteClient;

    public ClienteViewController(ClienteApiClient clienteClient) {
        this.clienteClient = clienteClient;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteClient.listar().stream()
                .sorted(Comparator.comparing(ClienteDto::getApellido, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList());
        return "clientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cliente", new ClienteDto());
        return VISTA_FORMULARIO;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("cliente", clienteClient.obtener(id));
        return VISTA_FORMULARIO;
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("cliente") ClienteDto cliente, Model model, RedirectAttributes flash) {
        if (!StringUtils.hasText(cliente.getDocumento()) || !StringUtils.hasText(cliente.getNombre())
                || !StringUtils.hasText(cliente.getApellido())) {
            model.addAttribute("error", "Documento, nombre y apellido son obligatorios.");
            return VISTA_FORMULARIO;
        }

        if (cliente.getIdCliente() == null) {
            clienteClient.crear(cliente);
            flash.addFlashAttribute("exito", "Cliente " + cliente.getNombre() + " registrado.");
        } else {
            clienteClient.actualizar(cliente.getIdCliente(), cliente);
            flash.addFlashAttribute("exito", "Cliente actualizado.");
        }
        return REDIRECT_LISTA;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Integer id, RedirectAttributes flash) {
        try {
            clienteClient.eliminar(id);
            flash.addFlashAttribute("exito", "Cliente eliminado.");
        } catch (FeignException.NotFound | FeignException.InternalServerError e) {
            flash.addFlashAttribute("error",
                    "No se pudo eliminar el cliente. Si tiene vehículos registrados, elimínalos primero.");
        }
        return REDIRECT_LISTA;
    }
}
