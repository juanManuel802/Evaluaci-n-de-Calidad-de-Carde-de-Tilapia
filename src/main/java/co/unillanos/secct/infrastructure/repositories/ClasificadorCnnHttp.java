package co.unillanos.secct.infrastructure.repositories;

import co.unillanos.secct.usecases.dto.PartePez;
import co.unillanos.secct.usecases.dto.ResultadoClasificacion;
import co.unillanos.secct.usecases.ports.ClasificadorCnnPort;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ClasificadorCnnHttp implements ClasificadorCnnPort {

    private final String baseUrl;
    private final HttpClient httpClient;

    public ClasificadorCnnHttp(String baseUrl) {
        this(baseUrl, HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build());
    }

    public ClasificadorCnnHttp(String baseUrl, HttpClient httpClient) {
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
    }

    @Override
    public List<ResultadoClasificacion> clasificar(byte[] imagen) {
        String imagenBase64 = Base64.getEncoder().encodeToString(imagen);
        String cuerpo = "{\"imagen_base64\":\"" + imagenBase64 + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/evaluar"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Error al comunicarse con el servicio CNN: " + e.getMessage(), e);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("El servicio CNN respondió con estado " + response.statusCode());
        }

        return parsearRespuesta(response.body());
    }

    private List<ResultadoClasificacion> parsearRespuesta(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray partes = root.getAsJsonArray("partes");

        List<ResultadoClasificacion> resultados = new ArrayList<>();
        for (JsonElement elemento : partes) {
            JsonObject parte = elemento.getAsJsonObject();
            PartePez partePez = PartePez.valueOf(parte.get("parte").getAsString());
            int categoriaNtc = parte.get("categoria_ntc").getAsInt();
            double confianza = parte.get("confianza").getAsDouble();
            resultados.add(new ResultadoClasificacion(partePez, categoriaNtc, confianza));
        }
        return resultados;
    }
}
