package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.EventType;
import co.unillanos.secct.entities.Log;
import co.unillanos.secct.infrastructure.repositories.EventTypeRepository;
import co.unillanos.secct.infrastructure.repositories.LogRepository;
import co.unillanos.secct.usecases.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock
    private LogRepository logRepository;

    @Mock
    private EventTypeRepository eventTypeRepository;

    private LogService logService;

    @BeforeEach
    void setUp() {
        logService = new LogService(logRepository, eventTypeRepository);
    }

    @Test
    @DisplayName("PU-35: LogService sanitiza y enmascara correos y teléfonos en description (RNF-SEC-04) y guarda Log con fecha actual")
    void testPU35_LogServiceMasksSensitiveDataAndSavesLog() {
        // Given
        EventType eventType = new EventType((short) 1, "USER_REGISTERED", "Registro de nuevo usuario");
        when(eventTypeRepository.findByCode("USER_REGISTERED")).thenReturn(Optional.of(eventType));

        String rawDescription = "Registro de usuario con correo juan.perez@unillanos.edu.co y teléfono 3109876543 en el sistema";
        OffsetDateTime beforeCall = OffsetDateTime.now().minusSeconds(1);

        // When
        logService.recordEvent("USER_REGISTERED", 50, null, null, rawDescription);

        // Then
        ArgumentCaptor<Log> logCaptor = ArgumentCaptor.forClass(Log.class);
        verify(logRepository).save(logCaptor.capture());
        Log savedLog = logCaptor.getValue();

        assertThat(savedLog).isNotNull();
        assertThat(savedLog.getEventType()).isEqualTo(eventType);
        assertThat(savedLog.getUserId()).isEqualTo(50);
        assertThat(savedLog.getBatchId()).isNull();
        assertThat(savedLog.getEvaluationId()).isNull();
        assertThat(savedLog.getOccurredAt()).isAfterOrEqualTo(beforeCall);

        // Verify that PII is masked and not present in cleartext
        assertThat(savedLog.getDescription()).doesNotContain("juan.perez@unillanos.edu.co");
        assertThat(savedLog.getDescription()).doesNotContain("3109876543");
        assertThat(savedLog.getDescription()).contains("***@***.***");
        assertThat(savedLog.getDescription()).contains("3***");
    }

    @Test
    @DisplayName("maskSensitiveData enmascara múltiples correos y teléfonos con formato internacional")
    void testMaskSensitiveDataMultipleFormats() {
        String input = "Contacto: maria@gmail.com o soporte@empresa.org con tel +57 3201234567 o 300 456 7890";
        String masked = logService.maskSensitiveData(input);

        assertThat(masked).doesNotContain("maria@gmail.com");
        assertThat(masked).doesNotContain("soporte@empresa.org");
        assertThat(masked).doesNotContain("3201234567");
        assertThat(masked).doesNotContain("300 456 7890");
        assertThat(masked).contains("***@***.***");
    }

    @Test
    @DisplayName("maskSensitiveData maneja cadenas nulas de forma segura")
    void testMaskSensitiveDataNull() {
        assertThat(logService.maskSensitiveData(null)).isNull();
    }

    @Test
    @DisplayName("recordEvent lanza BusinessException si el tipo de evento no existe")
    void testRecordEventUnknownEventType() {
        when(eventTypeRepository.findByCode("UNKNOWN_EVENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> logService.recordEvent("UNKNOWN_EVENT", 1, null, null, "desc"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Tipo de evento no encontrado: UNKNOWN_EVENT");
    }
}
