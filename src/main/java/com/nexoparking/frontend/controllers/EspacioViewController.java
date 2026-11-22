package com.nexoparking.frontend.controllers;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

import com.nexoparking.frontend.clients.EspacioApiClient;
import com.nexoparking.frontend.clients.TarifaApiClient;
import com.nexoparking.frontend.dtos.EspacioDto;
import com.nexoparking.frontend.dtos.RegistroParqueoDto;
import com.nexoparking.frontend.services.ParqueaderoServiceInterface;

@Controller
@RequestMapping("/espacios")
public class EspacioViewController {

    private static final String VISTA_FORMULARIO = "espacios/formulario";
    private static final String REDIRECT_LISTA = "redirect:/espacios";

    private final EspacioApiClient espacioClient;
    private final TarifaApiClient tarifaClient;
    private final ParqueaderoServiceInterface parqueaderoService;

    public EspacioViewController(EspacioApiClient espacioClient, TarifaApiClient tarifaClient,
                                 ParqueaderoServiceInterface parqueaderoService) {
        this.espacioClient = espacioClient;
        this.tarifaClient = tarifaClient;
        this.parqueaderoService = parqueaderoService;
    }

    @GetMapping
    public String listar(Model model) {
        Map<Integer, String> placaPorEspacio = parqueaderoService.registrosActivos().stream()
                .filter(r -> r.getIdEspacio() != null)
                .collect(Collectors.toMap(RegistroParqueoDto::getIdEspacio, RegistroParqueoDto::getPlaca, (a, b) -> a));

        model.addAttribute("espacios", parqueaderoService.espaciosOrdenados());
        model.addAttribute("placaPorEspacio", placaPorEspacio);
        return "espacios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        var espacios = espacioClient.listar();
        EspacioDto espacio = EspacioDto.builder()
                .idEspacio(siguiente(espacios.stream().map(EspacioDto::getIdEspacio).filter(Objects::nonNull).toList()))
                .numeroEspacio(siguiente(espacios.stream().map(EspacioDto::getNumeroEspacio).filter(Objects::nonNull).toList()))
                .nivel(1)
                .build();
        return mostrarFormulario(espacio, model);
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("espacio") EspacioDto espacio, Model model, RedirectAttributes flash) {
        if (espacio.getIdEspacio() == null || espacio.getNumeroEspacio() == null || espacio.getNivel() == null
                || !StringUtils.hasText(espacio.getTipoEspacio())) {
            model.addAttribute("error", "Todos los campos son obligatorios.");
            return mostrarFormulario(espacio, model);
        }
        // El backend solo acepta espacios nuevos en estado libre.
        espacio.setEstado(Boolean.TRUE);
        espacioClient.crear(espacio);
        flash.addFlashAttribute("exito", "Espacio " + espacio.getNumeroEspacio() + " creado.");
        return REDIRECT_LISTA;
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable("id") Integer id,
                                @RequestParam("disponible") boolean disponible,
                                RedirectAttributes flash) {
        boolean tieneVehiculo = parqueaderoService.registrosActivos().stream()
                .anyMatch(r -> id.equals(r.getIdEspacio()));
        if (tieneVehiculo) {
            flash.addFlashAttribute("error",
                    "Ese espacio tiene un vehículo adentro. Registra primero la salida desde Parqueo.");
            return REDIRECT_LISTA;
        }

        espacioClient.actualizarEstado(id, EspacioDto.builder().idEspacio(id).estado(disponible).build());
        flash.addFlashAttribute("exito", disponible ? "Espacio habilitado." : "Espacio marcado como no disponible.");
        return REDIRECT_LISTA;
    }

    private String mostrarFormulario(EspacioDto espacio, Model model) {
        model.addAttribute("espacio", espacio);
        model.addAttribute("tarifas", tarifaClient.listar());
        return VISTA_FORMULARIO;
    }

    private static int siguiente(java.util.List<Integer> valores) {
        return valores.stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
    }
}
