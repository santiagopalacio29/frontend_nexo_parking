package com.nexoparking.frontend.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.Test;

import feign.FeignException;
import feign.Request;
import feign.Response;

class ApiErrorTranslatorTest {

    private final ApiErrorTranslator translator = new ApiErrorTranslator("http://localhost:8080");

    private static FeignException errorConEstado(int estado) {
        Request request = Request.create(Request.HttpMethod.GET, "http://localhost:8080/api/clientes",
                Map.of(), null, StandardCharsets.UTF_8, null);
        Response response = Response.builder()
                .status(estado).reason("prueba").request(request).headers(Map.of()).build();
        return FeignException.errorStatus("ClienteApiClient#listar()", response);
    }

    @Test
    void traducir_400_indicaDatosInvalidos() {
        assertTrue(translator.traducir(errorConEstado(400)).startsWith("Datos inválidos"));
    }

    @Test
    void traducir_404_indicaQueNoExiste() {
        assertTrue(translator.traducir(errorConEstado(404)).contains("no existe"));
    }

    @Test
    void traducir_406_indicaQueNoSeAcepto() {
        assertTrue(translator.traducir(errorConEstado(406)).contains("no aceptó"));
    }

    @Test
    void traducir_500_indicaErrorInterno() {
        assertTrue(translator.traducir(errorConEstado(500)).contains("error interno"));
    }

    @Test
    void traducir_otroCodigo_muestraElCodigo() {
        assertEquals("Error 418 al comunicarse con el backend.", translator.traducir(errorConEstado(418)));
    }
}
