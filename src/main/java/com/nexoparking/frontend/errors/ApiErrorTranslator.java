package com.nexoparking.frontend.errors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import feign.FeignException;
import feign.RetryableException;


@Component
public class ApiErrorTranslator {

    private final String backendUrl;

    public ApiErrorTranslator(@Value("${nexo.api.base-url}") String backendUrl) {
        this.backendUrl = backendUrl;
    }

    public String traducir(FeignException exception) {
        if (exception instanceof RetryableException || exception.status() <= 0) {
            return "No se pudo conectar con el backend de Nexo Parking en " + backendUrl
                    + ". Verifica que esté encendido.";
        }
        return switch (exception.status()) {
            case 400 -> "Datos inválidos: revisa que los campos obligatorios estén completos.";
            case 404 -> "El registro solicitado no existe en el backend.";
            case 406 -> "El backend no aceptó la operación. Revisa que el dato no esté repetido "
                    + "y que los registros relacionados existan.";
            case 500 -> "El backend tuvo un error interno. Revisa la consola del backend.";
            default -> "Error " + exception.status() + " al comunicarse con el backend.";
        };
    }
}
