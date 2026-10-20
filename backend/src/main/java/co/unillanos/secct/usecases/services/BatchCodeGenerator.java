package co.unillanos.secct.usecases.services;

import co.unillanos.secct.usecases.exceptions.BusinessException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Builds batch codes with the format SECCT-AAAAMMDD-NNN. */
@Component
public class BatchCodeGenerator {

    private static final String PREFIX = "SECCT";
    private static final long MAX_DAILY_SEQUENCE = 999;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generate(LocalDate captureDate, long sequence) {
        if (sequence < 1 || sequence > MAX_DAILY_SEQUENCE) {
            throw new BusinessException(
                    "Se alcanzó el máximo de " + MAX_DAILY_SEQUENCE + " lotes para la fecha de captura "
                            + captureDate, 409);
        }
        return PREFIX + "-" + captureDate.format(DATE_FORMAT) + "-" + String.format("%03d", sequence);
    }
}
