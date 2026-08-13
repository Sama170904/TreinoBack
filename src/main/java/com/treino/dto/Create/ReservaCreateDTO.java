package com.treino.dto.Create;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaCreateDTO {
    @NotNull(message = "El ID de la clase es obligatorio")
    private Long claseId;
}
