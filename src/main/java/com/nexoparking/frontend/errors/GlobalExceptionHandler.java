package com.nexoparking.frontend.errors;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.support.RequestContextUtils;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;


@ControllerAdvice
public class GlobalExceptionHandler {

    private final ApiErrorTranslator translator;

    public GlobalExceptionHandler(ApiErrorTranslator translator) {
        this.translator = translator;
    }

    @ExceptionHandler(FeignException.class)
    public String manejarErrorDelBackend(FeignException exception, HttpServletRequest request, Model model) {
        String mensaje = translator.traducir(exception);

        if ("GET".equalsIgnoreCase(request.getMethod())) {
            model.addAttribute("mensaje", mensaje);
            model.addAttribute("estado", exception.status());
            model.addAttribute("url", exception.request() != null ? exception.request().url() : null);
            return "error-api";
        }

        RequestContextUtils.getOutputFlashMap(request).put("error", mensaje);
        return "redirect:" + seccionDe(request.getServletPath());
    }

    
    static String seccionDe(String ruta) {
        if (ruta == null || ruta.isBlank() || "/".equals(ruta)) {
            return "/";
        }
        String sinBarraInicial = ruta.startsWith("/") ? ruta.substring(1) : ruta;
        int fin = sinBarraInicial.indexOf('/');
        return "/" + (fin == -1 ? sinBarraInicial : sinBarraInicial.substring(0, fin));
    }
}
