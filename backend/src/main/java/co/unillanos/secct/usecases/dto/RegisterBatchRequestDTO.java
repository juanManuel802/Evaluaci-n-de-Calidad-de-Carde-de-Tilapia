package co.unillanos.secct.usecases.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Request body for POST /api/batches. */
public class RegisterBatchRequestDTO {

    @NotNull(message = "La fecha de captura es obligatoria")
    @PastOrPresent(message = "La fecha de captura no puede ser futura")
    public LocalDate captureDate;

    @NotNull(message = "El peso es obligatorio")
    @DecimalMin(value = "0.01", message = "El peso debe ser mayor que 0")
    @DecimalMax(value = "10000.00", message = "El peso no puede superar 10.000,00")
    @Digits(integer = 5, fraction = 2, message = "El peso admite máximo 2 decimales")
    public BigDecimal weightKg;

    @NotNull(message = "El número de unidades de muestra es obligatorio")
    @Min(value = 1, message = "Las unidades de muestra deben ser entre 1 y 500")
    @Max(value = 500, message = "Las unidades de muestra deben ser entre 1 y 500")
    public Integer sampleUnitCount;

    @Size(max = 500, message = "Las observaciones admiten máximo 500 caracteres")
    public String notes;

    @Override
    public String toString() {
        return "RegisterBatchRequestDTO{captureDate=" + captureDate + ", weightKg=" + weightKg
                + ", sampleUnitCount=" + sampleUnitCount + "}";
    }
}
