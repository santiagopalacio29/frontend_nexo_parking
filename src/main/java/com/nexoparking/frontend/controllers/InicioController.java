package com.nexoparking.frontend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.nexoparking.frontend.services.ParqueaderoServiceInterface;

@Controller
public class InicioController {

    private final ParqueaderoServiceInterface parqueaderoService;

    public InicioController(ParqueaderoServiceInterface parqueaderoService) {
        this.parqueaderoService = parqueaderoService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("resumen", parqueaderoService.obtenerResumen());
        return "index";
    }
}
