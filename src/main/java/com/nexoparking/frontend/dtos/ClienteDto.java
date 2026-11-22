package com.nexoparking.frontend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDto {
    private Integer idCliente;
    private String documento;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
}
