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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nexoparking.frontend.clients.ClienteApiClient;
import com.nexoparking.frontend.clients.TarifaApiClient;
import com.nexoparking.frontend.clients.VehiculoApiClient;
import com.nexoparking.frontend.dtos.VehiculoDto;

import feign.FeignException;

@Controller
@RequestMapping("/vehiculos")
public class VehiculoViewController {

    private static final String VISTA_FORMULARIO = "vehiculos/formulario";
    private static final String REDIRECT_LISTA = "redirect:/vehiculos";

    private final VehiculoApiClient vehiculoClient;
    private final ClienteApiClient clienteClient;
    private final TarifaApiClient tarifaClient;

    public VehiculoViewController(VehiculoApiClient vehiculoClient, ClienteApiClient clienteClient,
                                  TarifaApiClient tarifaClient) {
        this.vehiculoClient = vehiculoClient;
        this.clienteClient = clienteClient;
        this.tarifaClient = tarifaClient;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("vehiculos", vehiculoClient.listar().stream()
                .sorted(Comparator.comparing(VehiculoDto::getPlaca, Comparator.nullsLast(String::compareTo)))
                .toList());
        return "vehiculos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        return mostrarFormulario(new VehiculoDto(), false, model);
    }

    @GetMapping("/{placa}/editar")
    public String editar(@PathVariable("placa") String placa, Model model) {
        return mostrarFormulario(vehiculoClient.obtener(placa), true, model);
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("vehiculo") VehiculoDto vehiculo,
                          @RequestParam(name = "edicion", defaultValue = "false") boolean edicion,
                          Model model, RedirectAttributes flash) {
        if (!StringUtils.hasText(vehiculo.getPlaca()) || !StringUtils.hasText(vehiculo.getTipoVehiculo())
                || !StringUtils.hasText(vehiculo.getDocumentoCliente())) {
            model.addAttribute("error", "Placa, tipo de vehículo y propietario son obligatorios.");
            return mostrarFormulario(vehiculo, edicion, model);
        }

        if (edicion) {
            vehiculoClient.actualizar(vehiculo.getPlaca(), vehiculo);
            flash.addFlashAttribute("exito", "Vehículo " + vehiculo.getPlaca() + " actualizado.");
        } else {
            vehiculo.setPlaca(vehiculo.getPlaca().trim().toUpperCase());
            vehiculoClient.crear(vehiculo);
            flash.addFlashAttribute("exito", "Vehículo " + vehiculo.getPlaca() + " registrado.");
        }
        return REDIRECT_LISTA;
    }

    @PostMapping("/{placa}/eliminar")
    public String eliminar(@PathVariable("placa") String placa, RedirectAttributes flash) {
        try {
            vehiculoClient.eliminar(placa);
            flash.addFlashAttribute("exito", "Vehículo " + placa + " eliminado.");
        } catch (FeignException.NotFound | FeignException.InternalServerError e) {
            flash.addFlashAttribute("error",
                    "No se pudo eliminar el vehículo " + placa + ". Puede tener registros de parqueo asociados.");
        }
        return REDIRECT_LISTA;
    }

    private String mostrarFormulario(VehiculoDto vehiculo, boolean edicion, Model model) {
        model.addAttribute("vehiculo", vehiculo);
        model.addAttribute("edicion", edicion);
        model.addAttribute("clientes", clienteClient.listar());
        model.addAttribute("tarifas", tarifaClient.listar());
        return VISTA_FORMULARIO;
    }
}
