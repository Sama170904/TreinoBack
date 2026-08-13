package com.treino.dto.Create;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditoAsignarDTO {
    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "La cantidad de créditos es obligatoria")
    @Min(value = 1, message = "Debe asignar al menos 1 crédito")
    private Integer cantidad;

    @NotBlank(message = "El tipo de vigencia es obligatorio")
    private String vigenciaTipo;
}
