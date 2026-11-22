package com.nexoparking.frontend.controllers;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nexoparking.frontend.clients.FacturaApiClient;
import com.nexoparking.frontend.clients.ParqueoApiClient;
import com.nexoparking.frontend.dtos.FacturaDto;
import com.nexoparking.frontend.dtos.RegistroParqueoDto;
import com.nexoparking.frontend.services.ParqueaderoServiceInterface;

import feign.FeignException;

@Controller
@RequestMapping("/facturas")
public class FacturaViewController {

    private final FacturaApiClient facturaClient;
    private final ParqueoApiClient parqueoClient;
    private final ParqueaderoServiceInterface parqueaderoService;

    public FacturaViewController(FacturaApiClient facturaClient, ParqueoApiClient parqueoClient,
                                 ParqueaderoServiceInterface parqueaderoService) {
        this.facturaClient = facturaClient;
        this.parqueoClient = parqueoClient;
        this.parqueaderoService = parqueaderoService;
    }

    @GetMapping
    public String listar(Model model) {
        List<FacturaDto> facturas = facturaClient.listar().stream()
                .sorted(Comparator.comparing(FacturaDto::getFechaPago, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        double total = facturas.stream().map(FacturaDto::getValorTotal).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).sum();

        model.addAttribute("facturas", facturas);
        model.addAttribute("total", total);
        return "facturas/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable("id") Integer id, Model model) {
        FacturaDto factura = facturaClient.obtener(id);
        RegistroParqueoDto registro = buscarRegistro(factura.getIdRegistro());

        model.addAttribute("factura", factura);
        model.addAttribute("registro", registro);
        model.addAttribute("espacio", registro == null ? null
                : parqueaderoService.espaciosPorId().get(registro.getIdEspacio()));
        return "facturas/detalle";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Integer id, RedirectAttributes flash) {
        facturaClient.eliminar(id);
        flash.addFlashAttribute("exito", "Factura #" + id + " eliminada.");
        return "redirect:/facturas";
    }

    /** El registro es información complementaria: si no se encuentra, la factura se muestra igual. */
    private RegistroParqueoDto buscarRegistro(Integer idRegistro) {
        if (idRegistro == null) {
            return null;
        }
        try {
            return parqueoClient.obtener(idRegistro);
        } catch (FeignException.NotFound e) {
            return null;
        }
    }
}
