package com.nexoparking.frontend.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nexoparking.frontend.dtos.EntradaRequest;
import com.nexoparking.frontend.dtos.FacturaDto;
import com.nexoparking.frontend.dtos.RegistroParqueoDto;
import com.nexoparking.frontend.dtos.SalidaRequest;
import com.nexoparking.frontend.services.ParqueaderoServiceInterface;

import feign.FeignException;

/** Caso de uso principal: registrar entrada y salida de vehículos. */
@Controller
@RequestMapping("/parqueo")
public class ParqueoViewController {

    static final List<String> METODOS_PAGO = List.of("efectivo", "tarjeta", "transferencia", "nequi");
    private static final String REDIRECT_PARQUEO = "redirect:/parqueo";

    private final ParqueaderoServiceInterface parqueaderoService;

    public ParqueoViewController(ParqueaderoServiceInterface parqueaderoService) {
        this.parqueaderoService = parqueaderoService;
    }

    @GetMapping
    public String inicio(Model model) {
        model.addAttribute("activos", parqueaderoService.registrosActivos());
        model.addAttribute("historial", parqueaderoService.historial());
        model.addAttribute("espaciosLibres", parqueaderoService.espaciosLibres());
        model.addAttribute("espaciosPorId", parqueaderoService.espaciosPorId());
        model.addAttribute("vehiculosDisponibles", parqueaderoService.vehiculosFueraDelParqueadero());
        model.addAttribute("metodosPago", METODOS_PAGO);
        return "parqueo/index";
    }

    @PostMapping("/entrada")
    public String registrarEntrada(@RequestParam(name = "placa", required = false) String placa,
                                   @RequestParam(name = "idEspacio", required = false) Integer idEspacio,
                                   RedirectAttributes flash) {
        if (!StringUtils.hasText(placa) || idEspacio == null) {
            flash.addFlashAttribute("error", "Selecciona el vehículo y el espacio.");
            return REDIRECT_PARQUEO;
        }
        try {
            RegistroParqueoDto registro = parqueaderoService.registrarEntrada(new EntradaRequest(placa, idEspacio));
            flash.addFlashAttribute("exito", "Entrada registrada para " + registro.getPlaca()
                    + " (registro #" + registro.getIdRegistro() + ").");
        } catch (FeignException.NotAcceptable e) {
            flash.addFlashAttribute("error",
                    "No se pudo registrar la entrada. Verifica que el vehículo exista y que el espacio esté libre.");
        }
        return REDIRECT_PARQUEO;
    }

    @PostMapping("/{id}/salida")
    public String registrarSalida(@PathVariable("id") Integer idRegistro,
                                  @RequestParam(name = "metodoPago", required = false) String metodoPago,
                                  RedirectAttributes flash) {
        if (!StringUtils.hasText(metodoPago)) {
            flash.addFlashAttribute("error", "Selecciona el método de pago.");
            return REDIRECT_PARQUEO;
        }
        try {
            FacturaDto factura = parqueaderoService.registrarSalida(idRegistro, new SalidaRequest(metodoPago));
            flash.addFlashAttribute("exito", "Salida registrada. Se generó la factura.");
            return factura != null && factura.getIdFactura() != null
                    ? "redirect:/facturas/" + factura.getIdFactura()
                    : "redirect:/facturas";
        } catch (FeignException.NotAcceptable e) {
            flash.addFlashAttribute("error", "No se pudo registrar la salida. Puede que ya tenga salida "
                    + "o que no exista una tarifa para ese tipo de vehículo.");
            return REDIRECT_PARQUEO;
        }
    }
}
