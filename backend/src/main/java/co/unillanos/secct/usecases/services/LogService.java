package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.EventType;
import co.unillanos.secct.entities.Log;
import co.unillanos.secct.infrastructure.repositories.EventTypeRepository;
import co.unillanos.secct.infrastructure.repositories.LogRepository;
import co.unillanos.secct.usecases.exceptions.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.regex.Pattern;

/**
 * Cross-cutting Audit Log Service (DC-Log-transversal, RNF-SEC-04).
 */
@Service
public class LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(\\+?57)?[\\s-]?(3\\d{2})[\\s-]?(\\d{3})[\\s-]?(\\d{4})");

    private final LogRepository logRepository;
    private final EventTypeRepository eventTypeRepository;

    public LogService(LogRepository logRepository, EventTypeRepository eventTypeRepository) {
        this.logRepository = logRepository;
        this.eventTypeRepository = eventTypeRepository;
    }

    public void recordEvent(String eventCode, Integer userId, Integer batchId, Integer evaluationId,
                            String description) {
        EventType eventType = eventTypeRepository.findByCode(eventCode)
                .orElseThrow(() -> new BusinessException("Tipo de evento no encontrado: " + eventCode, 500));

        String sanitizedDescription = maskSensitiveData(description);
        OffsetDateTime now = OffsetDateTime.now();

        Log log = new Log(eventType, userId, batchId, evaluationId, now, sanitizedDescription);
        logRepository.save(log);

        logger.info("event={} userId={} batchId={} evaluationId={} description={}",
                eventCode, userId, batchId, evaluationId, sanitizedDescription);
    }

    /**
     * Sanitizes and masks any email or phone number in description (RNF-SEC-04).
     */
    public String maskSensitiveData(String description) {
        if (description == null) {
            return null;
        }
        String masked = EMAIL_PATTERN.matcher(description).replaceAll("***@***.***");
        masked = PHONE_PATTERN.matcher(masked).replaceAll("3***");
        return masked;
    }
}
