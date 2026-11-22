package com.nexoparking.frontend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntradaRequest {
    private String placa;
    private Integer idEspacio;
}
