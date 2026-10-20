package co.unillanos.secct.infrastructure.classifier;

import co.unillanos.secct.usecases.dto.ClassificationResult;
import co.unillanos.secct.usecases.exceptions.BusinessException;
import co.unillanos.secct.usecases.ports.Classifier;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

/** Calls the Python CNN service (POST /evaluar) and maps its answer to ClassificationResult. */
@Component
public class HttpCnnClassifier implements Classifier {

    private static final Logger logger = LoggerFactory.getLogger(HttpCnnClassifier.class);
    private static final String UNAVAILABLE_MESSAGE =
            "El servicio de clasificación no está disponible. Intenta de nuevo más tarde";
    private static final Set<String> EXPECTED_PARTS = Set.of("OJO", "PIEL");

    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final String baseUrl;

    public HttpCnnClassifier(@Value("${secct.cnn.url}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Override
    public List<ClassificationResult> classify(byte[] image) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("imagen_base64", Base64.getEncoder().encodeToString(image));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/evaluar"))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(UNAVAILABLE_MESSAGE, 503);
        } catch (IOException | IllegalArgumentException e) {
            logger.error("The CNN service did not respond: {}", e.getMessage());
            throw new BusinessException(UNAVAILABLE_MESSAGE, 503);
        }

        if (response.statusCode() != 200) {
            logger.error("The CNN service answered with status {}: {}", response.statusCode(), response.body());
            throw new BusinessException(UNAVAILABLE_MESSAGE, 503);
        }
        return parseResults(response.body());
    }

    private List<ClassificationResult> parseResults(String body) {
        try {
            JsonArray parts = gson.fromJson(body, JsonObject.class).getAsJsonArray("partes");
            List<ClassificationResult> results = new ArrayList<>();
            for (JsonElement element : parts) {
                JsonObject part = element.getAsJsonObject();
                String fishPartCode = part.get("parte").getAsString();
                short ntcCategory = part.get("categoria_ntc").getAsShort();
                BigDecimal confidence = part.get("confianza").getAsBigDecimal();
                if (!EXPECTED_PARTS.contains(fishPartCode) || ntcCategory < 1 || ntcCategory > 5
                        || confidence.signum() < 0 || confidence.compareTo(BigDecimal.ONE) > 0) {
                    throw new IllegalStateException("Unexpected value in the CNN answer: " + part);
                }
                results.add(new ClassificationResult(fishPartCode, ntcCategory, confidence));
            }
            boolean hasAllParts = results.stream().map(result -> result.fishPartCode).distinct().count()
                    == EXPECTED_PARTS.size() && results.size() == EXPECTED_PARTS.size();
            if (!hasAllParts) {
                throw new IllegalStateException("The CNN answer must contain OJO and PIEL once each");
            }
            return results;
        } catch (JsonParseException | IllegalStateException | NullPointerException | NumberFormatException
                 | ClassCastException e) {
            logger.error("Invalid answer from the CNN service: {}", e.getMessage());
            throw new BusinessException(UNAVAILABLE_MESSAGE, 503);
        }
    }
}
