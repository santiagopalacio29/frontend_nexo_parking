package com.nexoparking.frontend.controllers;

import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nexoparking.frontend.clients.TarifaApiClient;
import com.nexoparking.frontend.dtos.TarifaDto;

import feign.FeignException;

@Controller
@RequestMapping("/tarifas")
public class TarifaViewController {

    private static final String VISTA_FORMULARIO = "tarifas/formulario";
    private static final String REDIRECT_LISTA = "redirect:/tarifas";

    private final TarifaApiClient tarifaClient;

    public TarifaViewController(TarifaApiClient tarifaClient) {
        this.tarifaClient = tarifaClient;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tarifas", tarifaClient.listar());
        return "tarifas/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        int siguienteId = tarifaClient.listar().stream()
                .map(TarifaDto::getIdTarifa)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max().orElse(0) + 1;
        model.addAttribute("tarifa", TarifaDto.builder().idTarifa(siguienteId).build());
        model.addAttribute("edicion", false);
        return VISTA_FORMULARIO;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("tarifa", tarifaClient.obtener(id));
        model.addAttribute("edicion", true);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("tarifa") TarifaDto tarifa,
                          @RequestParam(name = "edicion", defaultValue = "false") boolean edicion,
                          Model model, RedirectAttributes flash) {
        if (tarifa.getIdTarifa() == null || !StringUtils.hasText(tarifa.getTipoVehiculo())
                || tarifa.getValorHora() == null || tarifa.getValorHora() <= 0) {
            model.addAttribute("error", "ID, tipo de vehículo y un valor por hora mayor a cero son obligatorios.");
            model.addAttribute("edicion", edicion);
            return VISTA_FORMULARIO;
        }

        tarifa.setTipoVehiculo(tarifa.getTipoVehiculo().trim().toLowerCase());
        if (edicion) {
            tarifaClient.actualizar(tarifa.getIdTarifa(), tarifa);
            flash.addFlashAttribute("exito", "Tarifa de " + tarifa.getTipoVehiculo() + " actualizada.");
        } else {
            tarifaClient.crear(tarifa);
            flash.addFlashAttribute("exito", "Tarifa de " + tarifa.getTipoVehiculo() + " creada.");
        }
        return REDIRECT_LISTA;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Integer id, RedirectAttributes flash) {
        try {
            tarifaClient.eliminar(id);
            flash.addFlashAttribute("exito", "Tarifa eliminada.");
        } catch (FeignException.NotFound | FeignException.InternalServerError e) {
            flash.addFlashAttribute("error", "No se pudo eliminar la tarifa.");
        }
        return REDIRECT_LISTA;
    }
}
