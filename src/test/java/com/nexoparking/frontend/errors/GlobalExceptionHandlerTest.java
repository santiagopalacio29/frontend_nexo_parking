package com.nexoparking.frontend.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GlobalExceptionHandlerTest {

    @Test
    void seccionDe_devuelveElPrimerSegmentoDeLaRuta() {
        assertEquals("/clientes", GlobalExceptionHandler.seccionDe("/clientes/5/eliminar"));
        assertEquals("/parqueo", GlobalExceptionHandler.seccionDe("/parqueo/entrada"));
        assertEquals("/tarifas", GlobalExceptionHandler.seccionDe("/tarifas"));
    }

    @Test
    void seccionDe_rutaVaciaVuelveAlInicio() {
        assertEquals("/", GlobalExceptionHandler.seccionDe("/"));
        assertEquals("/", GlobalExceptionHandler.seccionDe(null));
    }
}
