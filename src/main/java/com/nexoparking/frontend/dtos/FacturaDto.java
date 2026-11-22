package com.nexoparking.frontend.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaDto {
    private Integer idFactura;
    private Integer idRegistro;
    private LocalDateTime fechaPago;
    private Double valorTotal;
    private String metodoPago;
}
