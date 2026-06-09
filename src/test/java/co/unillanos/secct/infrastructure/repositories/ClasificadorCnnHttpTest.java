package co.unillanos.secct.infrastructure.repositories;

import co.unillanos.secct.usecases.dto.PartePez;
import co.unillanos.secct.usecases.dto.ResultadoClasificacion;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ClasificadorCnnHttpTest {

    private HttpServer server;
    private String baseUrl;
    private final AtomicReference<String> cuerpoPeticion = new AtomicReference<>();

    @BeforeEach
    void arrancarStub() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void detenerStub() {
        server.stop(0);
    }

    private void configurarStub(String jsonRespuesta) {
        server.createContext("/evaluar", ex -> {
            cuerpoPeticion.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = jsonRespuesta.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, body.length);
            try (var os = ex.getResponseBody()) { os.write(body); }
        });
        server.start();
    }

    @Test
    void mapea_una_parte_correctamente() {
        configurarStub("{\"partes\":[{\"parte\":\"OJO\",\"categoria_ntc\":3,\"confianza\":0.91}]}");

        ClasificadorCnnHttp clasificador = new ClasificadorCnnHttp(baseUrl);
        List<ResultadoClasificacion> resultados = clasificador.clasificar(new byte[]{1, 2, 3});

        assertEquals(1, resultados.size());
        assertEquals(PartePez.OJO, resultados.get(0).getParte());
        assertEquals(3, resultados.get(0).getCategoriaNtc());
        assertEquals(0.91, resultados.get(0).getPuntajeConfianza(), 0.001);
    }

    @Test
    void mapea_dos_partes_correctamente() {
        configurarStub(
            "{\"partes\":["
            + "{\"parte\":\"OJO\",\"categoria_ntc\":3,\"confianza\":0.91},"
            + "{\"parte\":\"PIEL\",\"categoria_ntc\":2,\"confianza\":0.85}"
            + "]}");

        ClasificadorCnnHttp clasificador = new ClasificadorCnnHttp(baseUrl);
        List<ResultadoClasificacion> resultados = clasificador.clasificar(new byte[]{1, 2, 3});

        assertEquals(2, resultados.size());
        assertEquals(PartePez.OJO,  resultados.get(0).getParte());
        assertEquals(PartePez.PIEL, resultados.get(1).getParte());
        assertEquals(2, resultados.get(1).getCategoriaNtc());
        assertEquals(0.85, resultados.get(1).getPuntajeConfianza(), 0.001);
    }

    @Test
    void envia_imagen_en_base64() {
        configurarStub("{\"partes\":[{\"parte\":\"OJO\",\"categoria_ntc\":3,\"confianza\":0.90}]}");

        ClasificadorCnnHttp clasificador = new ClasificadorCnnHttp(baseUrl);
        clasificador.clasificar(new byte[]{(byte) 0xFF, (byte) 0xD8});

        String cuerpo = cuerpoPeticion.get();
        assertNotNull(cuerpo);
        assertTrue(cuerpo.contains("imagen_base64"), "El cuerpo debe contener el campo imagen_base64");
        assertTrue(cuerpo.contains("/9g="), "El base64 de los bytes 0xFF 0xD8 debe ser /9g=");
    }

    @Test
    void lanza_excepcion_cuando_el_servicio_falla() {
        server.createContext("/evaluar", ex -> {
            ex.sendResponseHeaders(500, 0);
            ex.getResponseBody().close();
        });
        server.start();

        ClasificadorCnnHttp clasificador = new ClasificadorCnnHttp(baseUrl);
        assertThrows(RuntimeException.class, () -> clasificador.clasificar(new byte[]{1}));
    }
}
